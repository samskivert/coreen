//
// $Id$

package coreen.project;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.Page;

/**
 * Displays a single project.
 */
public class ProjectPage extends AbstractPage
{
    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.PROJECT;
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        // TODO
    }
}
