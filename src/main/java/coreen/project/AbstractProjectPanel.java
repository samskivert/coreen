//
// $Id$

package coreen.project;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Composite;

import coreen.client.Args;
import coreen.model.Project;
import coreen.rpc.ProjectService;
import coreen.rpc.ProjectServiceAsync;

/**
 * Handles different project panels.
 */
public abstract class AbstractProjectPanel extends Composite
{
    /** Returns the detail id of the current project panel. */
    public abstract ProjectPage.Detail getId ();

    /**
     * Called either immediately after this panel has been added to the UI hierarchy, or when the
     * panel has received new arguments, due to the user clicking on an internal link.
     */
    public abstract void setArgs (Project proj, Args args);

    /** A reference to our project service which any project panel will surely need. */
    protected static final ProjectServiceAsync _projsvc = GWT.create(ProjectService.class);
}
