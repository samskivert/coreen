//
// $Id$

package coreen.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOutHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Popups;

/**
 * Handles popping up on mouse over (after a delay or immediately), and ensuring that only one
 * popup in the group is visible at a given time.
 */
public class PopupGroup
{
    /** Allows the widget created by a {@link Thunk} to realign the popup if its size changes. */
    public interface Positioner {
        public void sizeDidChange ();
    }

    /** Used by {@link #bindPopup} to defer popup contents creation. */
    public interface Thunk {
        public Widget create (Positioner pos);
    }

    /**
     * Configures this popup group to show its popups above its target.
     */
    public PopupGroup showAbove ()
    {
        _showAbove = true;
        return this;
    }

    /**
     * Configures this popup group to show its popups below its target.
     */
    public PopupGroup showBelow ()
    {
        _showAbove = false;
        return this;
    }

    /**
     * Configures this group to show hover popups after the specified (millisecond) delay.
     */
    public PopupGroup setHoverDelay (int millis)
    {
        _hoverDelay = millis;
        return this;
    }

    /**
     * Binds listeners the specified target widget that will cause a popup to be created when the
     * mouse hovers over the widget for a time greater than or equal to the popup delay supplied to
     * this group at construction time.
     */
    public <T extends Widget & HasMouseOverHandlers & HasMouseOutHandlers> void bindHover (
        final T target, final Thunk contents)
    {
        Popper popper = new Popper(target, contents);
        target.addMouseOverHandler(popper);
        target.addMouseOutHandler(popper);
    }

    /**
     * Binds listeners to the specified target widget that will cause a popup to be created when
     * the widget is clicked.
     */
    public <T extends Widget & HasClickHandlers> void bindClick (
        final T target, final Thunk contents)
    {
        Popper popper = new Popper(target, contents);
        target.addClickHandler(popper);
        target.addStyleName(_rsrc.styles().actionable());
    }

    /**
     * Configures the showing popup for this group. Any previously showing popup will be hidden. In
     * general this method is only used internally, but a popup group can be used by externally
     * managed popups in which case this method is needed.
     */
    public void setShowing (PopupPanel showing)
    {
        if (_showing != null) {
            _showing.hide();
        }
        _showing = showing;
    }

    protected class Popper extends Timer implements MouseOverHandler, MouseOutHandler, ClickHandler
    {
        public Popper (Widget target, Thunk thunk) {
            _target = target;
            _thunk = thunk;
        }

        public void onMouseOver (MouseOverEvent event) {
            if (_hoverDelay == 0) {
                run();
            } else {
                schedule(_hoverDelay);
            }
        }

        public void onMouseOut (MouseOutEvent event) {
            cancel();
            // if (_popup != null) {
            //     _popup.hide();
            // }
        }

        public void onClick (ClickEvent event) {
            run();
        }

        public void run () {
            if (_popup == null) {
                _popup = new PopupPanel(true);
                _popup.setStyleName(_rsrc.styles().popup());
                _popup.setWidget(_thunk.create(new Positioner() {
                    public void sizeDidChange () {
                        positionPopup();
                    }
                }));
            }
            setShowing(_popup);
            positionPopup();
        }

        protected void positionPopup () {
            if (_showAbove) {
                UIUtil.showAbove(_popup, _target);
            } else {
                Popups.showBelow(_popup, _target);
            }
        }

        protected Widget _target;
        protected Thunk _thunk;
        protected PopupPanel _popup;
    }

    protected int _hoverDelay = DEFAULT_HOVER_DELAY;
    protected boolean _showAbove = true;
    protected PopupPanel _showing;

    protected static final UIResources _rsrc = GWT.create(UIResources.class);
    static {
        _rsrc.styles().ensureInjected();
    }

    protected static final int DEFAULT_HOVER_DELAY = 300;
}
