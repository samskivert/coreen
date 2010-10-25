//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Enables viewing of consoles (buffers of strings) which are written by the server and displayed
 * by the client.
 */
@RemoteServiceRelativePath(ConsoleService.ENTRY_POINT)
public interface ConsoleService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "console";

    /** Returned by {@link #fetchConsole}. */
    public static class ConsoleResult implements IsSerializable {
        /** The requested data. */
        public String[] lines;

        /** Whether or not the console is still open (may eventually receive more data). */
        public boolean isOpen;
    }

    /** Fetches data from the specified console.
     * @param fromLine an offset into the buffer at which to start fetching. */
    ConsoleResult fetchConsole (String id, int fromLine) throws ServiceException;
}
