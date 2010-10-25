//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Provides the asynchronous version of {@link ConsoleService}.
 */
public interface ConsoleServiceAsync
{
    /**
     * The async version of {@link ConsoleService#fetchConsole}.
     */
    void fetchConsole (String id, int fromLine, AsyncCallback<ConsoleService.ConsoleResult> callback);
}
