//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;

import com.threerings.gwt.ui.Bindings;
import com.threerings.gwt.ui.Widgets;
import com.threerings.gwt.util.Value;

/**
 * Handles the creation of a panel that hides non-public members behind a toggle.
 */
public abstract class NonPublicPanel extends FlowPanel
{
    public final Value<Boolean> showing = Value.create(false);

    /** A listener that can be added to all non-public members that shows this non-public members
     * panel whenever any of them are marked as showing. */
    public final Value.Listener<Boolean> syncer = new Value.Listener<Boolean>() {
        public void valueChanged (Boolean value) {
            if (value) {
                showing.update(true);
            }
        }
    };

    /** Creates an empty non-public panel and binds it to its showing trigger. */
    public NonPublicPanel ()
    {
        Bindings.bindVisible(showing, this);
    }

    /** Creates a toggle panel to be added above this non-public panel. */
    public FlowPanel makeToggle (String label)
    {
        return TogglePanel.makeTogglePanel(_rsrc.styles().nonPublic(), showing,
                                           Widgets.newLabel(label));
    }

    @Override
    public void setVisible (boolean visible)
    {
        if (visible && getWidgetCount() == 0) {
            populate();
        }
        super.setVisible(visible);
    }

    protected abstract void populate ();

    protected static final ProjectResources _rsrc = GWT.create(ProjectResources.class);
}
