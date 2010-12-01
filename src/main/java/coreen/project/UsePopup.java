//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*; // myriad Mouse bits
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.PopupCallback;
import com.threerings.gwt.util.WindowUtil;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.DefDetail;
import coreen.model.DefId;
import coreen.model.Kind;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.PopupGroup;
import coreen.ui.UIResources;
import coreen.ui.UIUtil;
import coreen.ui.WindowFX;
import coreen.util.DefMap;

/**
 * Displays information about a use when the mouse is hovered over it.
 */
public class UsePopup extends PopupPanel
{
    public static class Linker {
        public Hyperlink makeLink (DefDetail deet) {
            return Link.create(deet.sig, Page.PROJECT, prepArgs(deet));
        }

        public ClickHandler makeClick (DefDetail deet) {
            return Link.createHandler(Page.PROJECT, prepArgs(deet));
        }

        protected Object[] prepArgs (DefDetail deet) {
            List<Object> args = new ArrayList<Object>();
            args.add(deet.unit.projectId);
            args.add(_detail);
            addDetailArgs(deet, args);
            return args.toArray();
        }

        protected void addDetailArgs (DefDetail deet, List<Object> args) {
            for (DefId did : deet.path) {
                args.add(did.id);
            }
            args.add(deet.id);
        }

        protected Linker (ProjectPage.Detail detail) {
            _detail = detail;
        }

        protected ProjectPage.Detail _detail;
    }

    public static final Linker SOURCE = new Linker(ProjectPage.Detail.SRC) {
        protected void addDetailArgs (DefDetail deet, List<Object> args) {
            args.add(deet.unit.id);
            args.add(deet.id);
        }
    };

    public static final Linker TYPE = new Linker(ProjectPage.Detail.TYP) {
        protected void addDetailArgs (DefDetail deet, List<Object> args) {
            for (DefId did : deet.path) {
                if (did.kind != Kind.MODULE) {
                    args.add(did.id);
                }
            }
            args.add(deet.id);
        }
    };

    public static final Linker BY_TYPES = new Linker(ProjectPage.Detail.TPS);

    public static Linker byModsInProject (final long projectId)
    {
        return new Linker (ProjectPage.Detail.MDS) {
            public Hyperlink makeLink (DefDetail deet) {
                List<Object> args = new ArrayList<Object>();
                args.add(deet.unit.projectId);
                args.add(deet.unit.projectId == projectId ? _detail : ProjectPage.Detail.TYP);
                addDetailArgs(deet, args);
                return Link.create(deet.sig, Page.PROJECT, args.toArray());
            }
        };
    }

    public static class Popper implements ClickHandler, MouseOverHandler, MouseOutHandler
    {
        public Popper (long referentId, Widget target, Linker linker, DefMap defmap,
                       boolean addClick) {
            _referentId = referentId;
            _target = target;
            _defmap = defmap;
            _linker = linker;

            if (addClick && target instanceof HasClickHandlers) {
                ((HasClickHandlers)target).addClickHandler(this);
                UIUtil.makeActionable(target);
            }
            if (target instanceof HasMouseOverHandlers) {
                ((HasMouseOverHandlers)target).addMouseOverHandler(this);
                ((HasMouseOutHandlers)target).addMouseOutHandler(this);
            }
        }

        public Popper setGroup (PopupGroup group) {
            _group = group;
            return this;
        }

        public Popper setHighlight (boolean highlight) {
            _highlight = highlight;
            return this;
        }

        public void onClick (ClickEvent event) {
            SpanWidget def = _defmap.get(_referentId);
            if (def != null) {
                WindowFX.scrollToPos(WindowUtil.getScrollIntoView(def));
            } else if (_popup != null) {
                _popup.go();
            // } else {
            //     boolean debounce = (System.currentTimeMillis() - _lastPopdown < BOUNCE);
            //     if (!debounce && (_popup == null || !_popup.isShowing())) {
            //         GWT.log("Immediate!");
            //         showPopup();
            //     }
            }
        }

        public void onMouseOver (MouseOverEvent event) {
            if (_highlight && UseHighlighter.highlightTarget(
                    _defmap, _referentId, _target, false)) {
                return;
            }
            if ((_popup == null || !_popup.isShowing())) {
                _timer.schedule(500);
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            // cancel any pending pop timer
            _timer.cancel();

            // if we've highlighted our onscreen def, unhighlight it
            if (_highlight) {
                UseHighlighter.clearTarget(_defmap, _referentId);
            }
        }

        protected void showPopup () {
            if (_group != null) {
                _group.setShowing(_popup);
            }

            // if the def came into view while we were waiting, just highlight it
            if (_highlight && UseHighlighter.highlightTarget(
                    _defmap, _referentId, _target, true)) {
                return;
            }

            // if we already have our popup, then just show it
            if (_popup != null) {
                UIUtil.showAbove(_popup, _target);
                return;
            }

            // otherwise we have to fetch our referent details
            _projsvc.getDef(_referentId, new PopupCallback<DefDetail>(_target) {
                public void onSuccess (DefDetail deet) {
                    _popup = new UsePopup(Popper.this, deet, _linker, _defmap);
                    showPopup();
                }
            });
        }

        protected void poppedDown () {
            _lastPopdown = System.currentTimeMillis();
        }

        protected Timer _timer = new Timer() {
            @Override public void run () {
                showPopup();
            }
        };

        protected long _referentId;
        protected Widget _target;
        protected Linker _linker;
        protected DefMap _defmap;
        protected boolean _highlight = true;

        protected UsePopup _popup;
        protected long _lastPopdown;
        protected PopupGroup _group;

        protected static final long BOUNCE = 250L;
    }

    public void go ()
    {
        if (_link != null) {
            History.newItem(_link.getTargetHistoryToken());
            hide();
        }
    }

    protected UsePopup (Popper popper, DefDetail deet, Linker linker, DefMap defmap)
    {
        super(true);
        setStyleName(_ursrc.styles().popup());
        _popper = popper;

        _link = linker.makeLink(deet); // we just use this for its history token...

        Label dragger = UIUtil.makeFloatRight(new Label(" ▤ "));
        dragger.setTitle("Click and drag to move popup.");
        Popups.makeDraggable(dragger, this);
        setWidget(Widgets.newFlowPanel(dragger, DefUtil.createDefSummary(deet, defmap, linker)));
    }

    @Override // from PopupPanel
    protected void onUnload ()
    {
        super.onUnload();
        _popper.poppedDown();
    }

    protected Popper _popper;
    protected Hyperlink _link;

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
    protected static final UIResources _ursrc = GWT.create(UIResources.class);
}
