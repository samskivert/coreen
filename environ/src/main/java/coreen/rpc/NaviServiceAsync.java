//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import coreen.model.Def;

/**
 * Provides the asynchronous version of {@link NaviService}.
 */
public interface NaviServiceAsync
{
    /**
     * The async version of {@link NaviService#getToTypeDefs}.
     */
    void getToTypeDefs (long projectId, AsyncCallback<Def[]> callback);

    /**
     * The async version of {@link NaviService#getToMethodDefs}.
     */
    void getToMethodDefs (long projectId, AsyncCallback<Def[]> callback);
}
