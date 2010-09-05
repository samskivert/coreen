//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.RemoteService;

import coreen.model.Def;
import coreen.model.Def;
import coreen.model.Project;

/**
 * Provides basic navigation services.
 */
public interface NaviService extends RemoteService
{
    /** Returns all projects known to the system. */
    Project[] getProjects () throws ServiceException;

    /** Returns all module and type definitions for the specified project. */
    Def[] getToTypeDefs (long projectId) throws ServiceException;

    /** Returns all module, type and method definitions for the specified project. */
    Def[] getToMethodDefs (long projectId) throws ServiceException;
}
