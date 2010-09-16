//
// $Id$

package coreen.client;

import com.google.gwt.user.client.ui.Composite;

/**
 * An abstract base for our various pages.
 */
public abstract class AbstractPage extends Composite
{
    /** Returns the id of the current page. */
    public abstract Page getId ();

    /**
     * Called either immediately after this page has been added to the UI hierarchy, or when the
     * page has received new arguments, due to the user clicking on an internal link.
     */
    public abstract void setArgs (Args args);
}
