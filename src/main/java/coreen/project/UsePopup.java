//
// $Id$

package coreen.project;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*; // myriad Mouse bits
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.PopupCallback; // TODO: make a custom version that handles errors
import com.threerings.gwt.util.WindowUtil;

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.Def;
import coreen.model.DefDetail;
import coreen.model.TypedId;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;
import coreen.ui.WindowFX;
import coreen.util.DefMap;

/**
 * Displays information about a use when the mouse is hovered over it.
 */
public class UsePopup extends PopupPanel
{
    public static class Linker {
        public Hyperlink makeLink (DefDetail deet) {
            List<Object> args = new ArrayList<Object>();
            args.add(deet.unit.projectId);
            args.add(_detail);
            addDetailArgs(deet, args);
            return Link.create(deet.sig, Page.PROJECT, args.toArray());
        }

        protected void addDetailArgs (DefDetail deet, List<Object> args) {
            for (TypedId tid : deet.path) {
                if (tid.type != Def.Type.MODULE) {
                    args.add(tid.id);
                }
            }
            args.add(deet.def.id);
        }

        protected Linker (ProjectPage.Detail detail) {
            _detail = detail;
        }

        protected ProjectPage.Detail _detail;
    }

    public static final Linker SOURCE = new Linker(ProjectPage.Detail.SRC) {
        protected void addDetailArgs (DefDetail deet, List<Object> args) {
            args.add(deet.unit.id);
            args.add(deet.def.id);
        }
    };

    public static final Linker TYPE = new Linker(ProjectPage.Detail.TYP);

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
                target.addStyleName(_rsrc.styles().actionable());
            }
            if (target instanceof HasMouseOverHandlers) {
                ((HasMouseOverHandlers)target).addMouseOverHandler(this);
                ((HasMouseOutHandlers)target).addMouseOutHandler(this);
            }
        }

        public void onClick (ClickEvent event) {
            Widget def = _defmap.get(_referentId);
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
            if (!highlightTarget() && (_popup == null || !_popup.isShowing())) {
                _timer.schedule(500);
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            // cancel any pending pop timer
            _timer.cancel();

            // if we've highlighted our onscreen def, unhighlight it
            Widget def = _defmap.get(_referentId);
            if (def != null) {
                def.removeStyleName(_rsrc.styles().highlight());
            }
        }

        protected boolean highlightTarget () {
            Widget def = _defmap.get(_referentId);
            if (def != null && WindowUtil.isScrolledIntoView(def)) {
                def.addStyleName(_rsrc.styles().highlight());
                return true;
            }
            return false;
        }

        protected void showPopup () {
            hidePopup();

            // if the def came into view while we were waiting, just highlight it
            if (highlightTarget()) {
                return;
            }

            // if we already have our popup, then just show it
            if (_popup != null) {
                _current = _popup;
                _popup.showNear(_target);
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

        protected void hidePopup () {
            if (_current != null) {
                _current.hide();
            }
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

        protected UsePopup _popup;
        protected long _lastPopdown;

        protected static final long BOUNCE = 250L;
    }

    public void showNear (Widget target)
    {
        setVisible(false);
        show();
        int left = target.getAbsoluteLeft();
        int top = Math.max(target.getAbsoluteTop() - getOffsetHeight(), 0);
        if (left + getOffsetWidth() > Window.getClientWidth()) {
            left = Math.max(0, Window.getClientWidth() - getOffsetWidth());
        }
        setPopupPosition(left, top);
        setVisible(true);
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
        setStyleName(_rsrc.styles().usePopup());
        _popper = popper;

        FlowPanel panel = new FlowPanel();
        panel.add(new TypeLabel(deet.path, deet.def, linker, defmap, deet.doc));
        Widget sig;
        if (deet.unit.projectId > 0) {
            sig = (_link = linker.makeLink(deet));
        } else {
            sig = Widgets.newLabel(deet.sig);
        }
        sig.addStyleName(_rsrc.styles().code());
        panel.add(sig);
        setWidget(panel);
    }

    @Override // from PopupPanel
    protected void onUnload ()
    {
        super.onUnload();
        _popper.poppedDown();
    }

    protected Popper _popper;
    protected Hyperlink _link;

    protected static UsePopup _current;

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
