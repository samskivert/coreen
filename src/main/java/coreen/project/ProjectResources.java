//
// $Id$

package coreen.project;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * Resources shared among project pages.
 */
public interface ProjectResources extends ClientBundle
{
    /** Defines our shared CSS styles. */
    public interface ProjectStyles extends CssResource {
        String code ();
        String def ();
        String use ();
        String usePopup ();
        String actionable ();
        String openDef ();
    }

    @Source("project.css")
    ProjectStyles styles ();
}
