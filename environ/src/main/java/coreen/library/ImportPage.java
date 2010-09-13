//
// $Id$

package coreen.library;

import coreen.client.AbstractPage;
import coreen.client.Args;
import coreen.client.Page;

/**
 * Displays a UI for importing projects.
 */
public class ImportPage extends AbstractPage
{
    @Override // from AbstractPage
    public Page getId ()
    {
        return Page.IMPORT;
    }

    @Override // from AbstractPage
    public void setArgs (Args args)
    {
        // TODO
    }
}
