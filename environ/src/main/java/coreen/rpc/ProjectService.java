//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import coreen.model.Project;

/**
 * Provides project-related services.
 */
@RemoteServiceRelativePath(ProjectService.ENTRY_POINT)
public interface ProjectService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "project";

    /** Returns metadata for the specified project. */
    Project getProject (long id) throws ServiceException;
}
