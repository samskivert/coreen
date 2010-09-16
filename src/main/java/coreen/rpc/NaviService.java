//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import coreen.model.Def;

/**
 * Provides basic navigation services.
 */
@RemoteServiceRelativePath(NaviService.ENTRY_POINT)
public interface NaviService extends RemoteService
{
    /** The path at which this service's servlet is mapped. */
    public static final String ENTRY_POINT = "navi";

    /** Returns all module and type definitions for the specified project. */
    Def[] getToTypeDefs (long projectId) throws ServiceException;

    /** Returns all module, type and method definitions for the specified project. */
    Def[] getToMethodDefs (long projectId) throws ServiceException;
}
