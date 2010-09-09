//
// $Id$

package coreen.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import com.threerings.gwt.ui.Widgets;

import coreen.rpc.NaviService;
import coreen.rpc.NaviServiceAsync;

/**
 * The main entry point for the Coreen GWT client.
 */
public class CoreenClient implements EntryPoint
{
    // from interface EntryPoint
    public void onModuleLoad () {
        setContent(Widgets.newLabel("Hello world!")); // TODO
    }

    protected void setContent (Widget content) {
        if (_content != null) {
            RootPanel.get(CLIENT_DIV).remove(_content);
        }
        _content = content;
        if (_content != null) {
            RootPanel.get(CLIENT_DIV).add(_content);
        }
    }

    protected Widget _content;

    protected static final NaviServiceAsync _navisvc = GWT.create(NaviService.class);
    protected static final String CLIENT_DIV = "client";
}
