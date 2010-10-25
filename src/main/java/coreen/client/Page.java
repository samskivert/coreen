//
// $Id$

package coreen.client;

import coreen.client.ColophonPage;
import coreen.config.EditProjectPage;
import coreen.library.ImportPage;
import coreen.library.LibraryPage;
import coreen.project.ProjectPage;

/**
 * Defines all of the top-level pages in the Coreen client.
 */
public enum Page
{
    /** Displays all known projects. */
    LIBRARY {
        public AbstractPage create () { return new LibraryPage(); }
    },

    /** Displays interface for adding projects. */
    IMPORT {
        public AbstractPage create () { return new ImportPage(); }
    },

    /** Displays a single project. */
    PROJECT {
        public AbstractPage create () { return new ProjectPage(); }
    },

    /** Displays a metadata editing interface for a single project. */
    EDIT {
        public AbstractPage create () { return new EditProjectPage(); }
    },

    /** Displays information about Coreen. */
    COLOPHON {
        public AbstractPage create () { return new ColophonPage(); }
    };

    /** Create a blank instance of the page represented by this enum. */
    public abstract AbstractPage create ();
}
