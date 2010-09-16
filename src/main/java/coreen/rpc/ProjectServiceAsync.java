//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import coreen.model.Project;

/**
 * Provides the asynchronous version of {@link ProjectService}.
 */
public interface ProjectServiceAsync
{
    /**
     * The async version of {@link ProjectService#getProject}.
     */
    void getProject (long id, AsyncCallback<Project> callback);

    /**
     * The async version of {@link ProjectService#updateProject}.
     */
    void updateProject (long id, AsyncCallback<Void> callback);
}
