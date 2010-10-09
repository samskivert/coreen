//
// $Id$

package coreen.rpc;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import coreen.model.Def;
import coreen.model.PendingProject;
import coreen.model.Project;
import coreen.model.TypedId;

/**
 * Provides library-related services.
 */
@RemoteServiceRelativePath(LibraryService.ENTRY_POINT)
public interface LibraryService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "library";

    /** Used by {@link #search}. */
    public static class SearchResult implements Serializable
    {
        /** The id of the project in which this result lives. */
        public long projectId;

        /** The name of the project in which this result lives. */
        public String project;

        /** The path to the result. */
        public TypedId[] path;

        /** The result in question. */
        public Def def;

        /** The documentation for the result in question. */
        public String doc;

        /** Creates and populates a search result. */
        public SearchResult (long projectId, String project, TypedId[] path, Def def, String doc) {
            this.projectId = projectId;
            this.project = project;
            this.path = path;
            this.def = def;
            this.doc = doc;
        }

        /** Used when unserializing. */
        public SearchResult () {}
    }

    /** Returns all projects known to the system. */
    Project[] getProjects () throws ServiceException;

    /** Returns all projects in the process of being imported. */
    PendingProject[] getPendingProjects () throws ServiceException;

    /** Initiates the import of a project with the specified source. Heuristics will be applied to
     * decipher the meaning of the supplied source. (Eventually) supported data include: path to
     * directory on the local filesystem, path to jar, tgz, zip, etc. file on the local filesystem,
     * version control URL (svn:, git:, http:, etc.), URL to archive file. */
    PendingProject importProject (String source) throws ServiceException;

    /** Searches for defs that match the specified query. */
    SearchResult[] search (String query) throws ServiceException;
}
