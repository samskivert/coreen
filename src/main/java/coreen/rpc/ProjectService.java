//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import coreen.model.CompUnit;
import coreen.model.Project;

/**
 * Provides project-related services.
 */
@RemoteServiceRelativePath(ProjectService.ENTRY_POINT)
public interface ProjectService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "project";

    /** Returns metadata for the specified project.
     * @throws ServiceException with e.no_such_project if project unknown. */
    Project getProject (long id) throws ServiceException;

    /** Requests that the specified project be updated.
     * @throws ServiceException with e.no_such_project if project unknown. */
    void updateProject (long id) throws ServiceException;

    /** Returns all compilation units associated with the specified project. */
    CompUnit[] getCompUnits (long projectId) throws ServiceException;
}
