//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

import coreen.util.ClickCallback;

/**
 * Handles showing a panel on click of a label, and allows programmatic triggering of same.
 */
public abstract class ShowCallback<T, D extends Widget> extends ClickCallback<T>
{
    /**
     * Shows the display associated with this callback. If the display is already showing, {@link
     * #displayShown} will be called immediately, otherwise the service request will be made and
     * the display created when the data retrieved (and {@link #displayShown} called at that time).
     */
    public void show ()
    {
        if (_display == null) {
            takeAction(false);
        } else {
            displayShown(); // already showing
        }
    }

    protected ShowCallback (Label trigger, FlowPanel target)
    {
        super(trigger);
        _target = target;
    }

    @Override // from ClickCallback
    protected void takeAction (boolean confirmed)
    {
        if (_display != null) {
            _target.remove(_display);
            ((Widget)_trigger).removeStyleName(_rsrc.styles().openDef());
            displayHidden();
            _display = null;
        } else {
            super.takeAction(confirmed);
        }
    }

    @Override // from ClickCallback
    protected boolean gotResult (T detail)
    {
        ((Widget)_trigger).addStyleName(_rsrc.styles().openDef());
        _target.add(_display = createDisplay(detail));
        displayShown();
        return true;
    }

    /**
     * Creates the display widget given the supplied details.
     */
    protected abstract D createDisplay (T detail);

    /**
     * Called after the display has been shown. Overriders should be sure to call super.
     */
    protected void displayShown ()
    {
        // TODO: scroll _display into view
    }

    /**
     * Called when the display has been hidden.
     */
    protected void displayHidden ()
    {
        // nada by default
    }

    protected FlowPanel _target;
    protected D _display;

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
