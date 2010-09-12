//
// $Id$

package coreen.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

import coreen.project.ProjectPage;
import coreen.library.ImportPage;
import coreen.library.LibraryPage;

/**
 * The main entry point for the Coreen GWT client.
 */
public class CoreenClient implements EntryPoint, ValueChangeHandler<String>
{
    // from interface EntryPoint
    public void onModuleLoad ()
    {
        History.addValueChangeHandler(this);
        History.fireCurrentHistoryState();
    }

    // from interface ValueChangeHandler<String>
    public void onValueChange (ValueChangeEvent<String> event)
    {
        Args args = new Args(event.getValue());

        // // if we have showing popups, clear them out
        // _pstack.clear();

        switch (args.page) {
        default:
        case LIBRARY:
            setPage(new LibraryPage(), args);
            break;
        case IMPORT:
            setPage(new ImportPage(), args);
            break;
        case PROJECT:
            setPage(new ProjectPage(), args);
            break;
        case COLOPHON:
            setPage(new ColophonPage(), args);
            break;
        }
    }

    protected void setPage (AbstractPage page, Args args)
    {
        if (_page == null || _page.getClass() != page.getClass()) {
            if (_page != null) {
                RootPanel.get(CLIENT_DIV).remove(_page);
                _page = null;
            }
            _page = page;
            if (_page != null) {
                RootPanel.get(CLIENT_DIV).add(_page);
            }
        }
        _page.setArgs(args);
    }

    protected AbstractPage _page;

    protected static final String CLIENT_DIV = "client";
}
