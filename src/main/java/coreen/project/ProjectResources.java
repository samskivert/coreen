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
        String defModule ();
        String defType ();
        String defFunc ();
        String defTerm ();
        String defUnknown ();
        String use ();
        String doc ();
        String usePopup ();
        String actionable ();
        String openDef ();
        String highlight ();
        String indent ();
        String typeLabel ();
        String /*typeLabel*/ Module ();
        String /*typeLabel*/ Type ();
        String typeIcon ();
        String defLabel ();
        String defClear ();
    }

    @Source("project.css")
    ProjectStyles styles ();
}
