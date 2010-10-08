//
// $Id$

package coreen.project;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.resources.client.ImageResource;

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
        String highlight ();
        String indent ();
        String typeLabel ();
    }

    @Source("project.css")
    ProjectStyles styles ();
}
