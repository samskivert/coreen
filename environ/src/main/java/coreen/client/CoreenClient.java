//
// $Id$

package coreen.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

import coreen.rpc.NaviService;

/**
 * The main entry point for the Coreen GWT client.
 */
public class CoreenClient extends EntryPoint
{
    // from interface EntryPoint
    public void onModuleLoad () {
        // TODO: things!
    }

    protected static final NaviServiceAsync _navisvc = GWT.create(NaviService.class);
}
