//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*; // myriad Mouse bits
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.PopupCallback; // TODO: make a custom version that handles errors

import coreen.client.Link;
import coreen.client.Page;
import coreen.model.DefDetail;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;

/**
 * Displays information about a use when the mouse is hovered over it.
 */
public class UsePopup extends PopupPanel
{
    public static class Popper implements MouseDownHandler, MouseOverHandler, MouseOutHandler
    {
        public Popper (SourcePanel.Styles styles, long referentId, Widget target) {
            _styles = styles;
            _referentId = referentId;
            _target = target;

            if (!(target instanceof HasMouseOverHandlers)) {
                GWT.log("Can't listen for mouse over on " + target);
            } else {
                ((HasMouseDownHandlers)target).addMouseDownHandler(this);
                ((HasMouseOverHandlers)target).addMouseOverHandler(this);
                ((HasMouseOutHandlers)target).addMouseOutHandler(this);
            }
        }

        public void onMouseDown (MouseDownEvent event) {
            boolean debounce = (System.currentTimeMillis() - _lastPopdown < BOUNCE);
            if (!debounce && (_popup == null || !_popup.isShowing())) {
                showPopup();
            }
        }

        public void onMouseOver (MouseOverEvent event) {
            if (_popup == null || !_popup.isShowing()) {
                _timer.schedule(500);
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            _timer.cancel();
        }

        protected void showPopup () {
            if (_popup != null) {
                _popup.show();
            } else {
                _projsvc.getDef(_referentId, new PopupCallback<DefDetail>(_target) {
                    public void onSuccess (DefDetail deet) {
                        Popups.showNear(_popup = new UsePopup(Popper.this, _styles, deet), _target);
                    }
                });
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

        protected SourcePanel.Styles _styles;
        protected long _referentId;
        protected Widget _target;
        protected UsePopup _popup;
        protected long _lastPopdown;

        protected static final long BOUNCE = 250L;
    }

    protected UsePopup (Popper popper, SourcePanel.Styles styles, DefDetail deet)
    {
        super(true);
        setStyleName(styles.usePopup());
        _popper = popper;

        if (deet.projectId > 0) {
            add(Link.create(deet.sig, Page.PROJECT, deet.projectId,
                            ProjectPage.Detail.SRC, deet.unitId, deet.def.id));
        } else {
            add(Widgets.newLabel(deet.sig));
        }
    }

    @Override // from PopupPanel
    protected void onUnload ()
    {
        super.onUnload();
        _popper.poppedDown();
    }

    protected Popper _popper;

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
