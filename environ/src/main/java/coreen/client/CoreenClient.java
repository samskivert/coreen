//
// $Id$

package coreen.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

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

        if (_page == null || _page.getId() != args.page) {
            if (_page != null) {
                RootPanel.get(CLIENT_DIV).remove(_page);
            }
            RootPanel.get(CLIENT_DIV).add(_page = args.page.create());
        }
        _page.setArgs(args);
    }

    protected AbstractPage _page;

    protected static final String CLIENT_DIV = "client";
}
