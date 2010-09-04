//
// $Id$

package coreen.rpc;

import java.util.List;

import com.google.gwt.user.client.rpc.RemoteService;

/**
 * Provides basic navigation services.
 */
public interface NaviService extends RemoteService
{
    /** Returns all projects known to the system. */
    List<Project> getProjects () throws ServiceException;

    /** Returns a list of all module and type definitions for the specified project. */
    List<Def> getToTypeDefs (long projectId) throws ServiceException;

    /** Returns a list of all module, type and method definitions for the specified project. */
    List<Def> getToMethodDefs (long projectId) throws ServiceException;
}
