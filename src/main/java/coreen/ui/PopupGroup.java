//
// $Id$

package coreen.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Handles popping up on mouse over (after a delay or immediately), and ensuring that only one
 * popup in the group is visible at a given time.
 */
public class PopupGroup
{
    /** Used by {@link #bindPopup} to defer popup contents creation. */
    public interface Thunk {
        public Widget create ();
    }

    /**
     * Creates a popup group that shows its popups after the mouse has been hovering for the
     * specifide number of millseconds.
     */
    public PopupGroup (int popDelay)
    {
        _popDelay = popDelay;
    }

    /**
     * Binds a popup to the
     */
    public <T extends Widget & HasMouseOverHandlers & HasMouseOutHandlers> void bindPopup (
        final T target, final Thunk contents)
    {
        Popper popper = new Popper(target, contents);
        target.addMouseOverHandler(popper);
        target.addMouseOutHandler(popper);
    }

    protected class Popper extends Timer implements MouseOverHandler, MouseOutHandler
    {
        public Popper (Widget target, Thunk thunk) {
            _target = target;
            _thunk = thunk;
        }

        public void onMouseOver (MouseOverEvent event) {
            if (_popDelay == 0) {
                run();
            } else {
                schedule(_popDelay);
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            cancel();
            // if (_popup != null) {
            //     _popup.hide();
            // }
        }

        public void run () {
            if (_popup == null) {
                _popup = new PopupPanel(true);
                _popup.setStyleName(_rsrc.styles().popup());
                _popup.setWidget(_thunk.create());
            }
            if (_showing != null) {
                _showing.hide();
            }
            _showing = _popup;
            UIUtil.showAbove(_popup, _target);
        }

        protected Widget _target;
        protected Thunk _thunk;
        protected PopupPanel _popup;
    }

    protected int _popDelay;
    protected PopupPanel _showing;

    protected static final UIResources _rsrc = GWT.create(UIResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }
}
