//
// $Id$

package coreen.ui;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources shared among UI components.
 */
public interface UIResources extends ClientBundle
{
    /** Defines our shared CSS styles. */
    public interface Styles extends CssResource {
        String console ();
        String popup ();
        String clear ();
    }

    @Source("ui.css")
    Styles styles ();
}
