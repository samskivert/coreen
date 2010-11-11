//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.AsyncCallback;
import coreen.model.PendingProject;
import coreen.model.Project;

/**
 * Provides the asynchronous version of {@link LibraryService}.
 */
public interface LibraryServiceAsync
{
    /**
     * The async version of {@link LibraryService#search}.
     */
    void search (String query, AsyncCallback<LibraryService.SearchResult[]> callback);

    /**
     * The async version of {@link LibraryService#getProjects}.
     */
    void getProjects (AsyncCallback<Project[]> callback);

    /**
     * The async version of {@link LibraryService#getPendingProjects}.
     */
    void getPendingProjects (AsyncCallback<PendingProject[]> callback);

    /**
     * The async version of {@link LibraryService#importProject}.
     */
    void importProject (String source, AsyncCallback<PendingProject> callback);
}
