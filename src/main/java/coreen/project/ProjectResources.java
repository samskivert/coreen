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
        String defModule ();
        String defType ();
        String defFunc ();
        String defTerm ();
        String defUnknown ();

        String useModule ();
        String useType ();
        String useFunc ();
        String useTerm ();
        String useUnknown ();

        String highModule ();
        String highType ();
        String highFunc ();
        String highTerm ();
        String highUnknown ();

        String use ();
        String doc ();
        String shortDoc ();
        String popDoc ();
        String borderTop ();
        String selected ();
        String openDef ();
        String highlight ();
        String indent ();

        String typeLabel ();
        String typeLabelHeader ();
        String /*typeLabel*/ Type ();
        String belowTypeLabel ();
        String typeIcon ();
        String typeIconBase ();
        String typeIconUR ();
        String typeIconLL ();
        String defLabel ();
        String toggle ();
        String nested ();
    }

    @Source("project.css")
    ProjectStyles styles ();
}
