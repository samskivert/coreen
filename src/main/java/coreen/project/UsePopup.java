//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.*; // myriad Mouse bits
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
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
        public Popper (long referentId, Widget target) {
            _referentId = referentId;
            _target = target;

            if (!(target instanceof HasMouseOverHandlers)) {
                GWT.log("Can't listen for mouse over on " + target);
            } else {
                ((HasMouseDownHandlers)target).addMouseDownHandler(this);
                ((HasMouseOverHandlers)target).addMouseOverHandler(this);
                ((HasMouseOutHandlers)target).addMouseOutHandler(this);
                target.addStyleName(_rsrc.styles().actionable());
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
                // _timer.schedule(100);
                showPopup();
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            _timer.cancel();
        }

        protected void showPopup () {
            if (_popup != null) {
                if (_current != null) {
                    _current.hide();
                }
                _current = _popup;
                _popup.showNear(_target);
                return;
            }

            _projsvc.getDef(_referentId, new PopupCallback<DefDetail>(_target) {
                public void onSuccess (DefDetail deet) {
                    _popup = new UsePopup(Popper.this, deet);
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
        protected UsePopup _popup;
        protected long _lastPopdown;

        protected static final long BOUNCE = 250L;
    }

    public void showNear (Widget target)
    {
        setVisible(false);
        show();
        int left = target.getAbsoluteLeft();
        int top = target.getAbsoluteTop() - getOffsetHeight() - 5;
        if (left + getOffsetWidth() > Window.getClientWidth()) {
            left = Math.max(0, Window.getClientWidth() - getOffsetWidth());
        }
        setPopupPosition(left, top);
        setVisible(true);
    }

    protected UsePopup (Popper popper, DefDetail deet)
    {
        super(true);
        setStyleName(_rsrc.styles().usePopup());
        _popper = popper;

        FlowPanel panel = new FlowPanel();
        if (deet.doc != null) {
            panel.add(Widgets.newHTML(deet.doc));
        }
        Widget sig;
        if (deet.projectId > 0) {
            sig = Link.create(deet.sig, Page.PROJECT, deet.projectId,
                              ProjectPage.Detail.SRC, deet.unitId, deet.def.id);
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

    protected static UsePopup _current;

    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
