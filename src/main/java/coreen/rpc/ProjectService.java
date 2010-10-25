//
// $Id$

package coreen.rpc;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import coreen.model.CompUnit;
import coreen.model.CompUnitDetail;
import coreen.model.Def;
import coreen.model.DefContent;
import coreen.model.DefDetail;
import coreen.model.Project;
import coreen.model.TypeDetail;
import coreen.model.TypeSummary;

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

    /** Updates the metadata for the supplied project.
     * @throws ServiceException with e.no_such_project if project unknown. */
    void updateProject (Project p) throws ServiceException;

    /** Requests that the specified project be updated.
     * @throws ServiceException with e.no_such_project if project unknown. */
    void rebuildProject (long id) throws ServiceException;

    /** Requests that the specifide project be deleted.
     * @throws ServiceException with e.no_such_project if project unknown. */
    void deleteProject (long id) throws ServiceException;

    /** Returns all compilation units associated with the specified project. */
    CompUnit[] getCompUnits (long projectId) throws ServiceException;

    /** Returns all modules in the specified project and their immediate members. The results are
     * a 2D array where each subarray is a module def and its members in sorted order. */
    Def[][] getModsAndMembers (long projectId) throws ServiceException;

    /** Returns all modules in the specified project. */
    Def[] getModules (long projectId) throws ServiceException;

    /** Returns all of the types defined by this project. */
    Def[] getTypes (long projectId) throws ServiceException;

    /** Returns all defs that are immediate children of the specified def. */
    Def[] getMembers (long defId) throws ServiceException;

    /** Returns details for the specified compilation unit.
     * @throws ServiceException with e.no_such_unit if the unit is unknown. */
    CompUnitDetail getCompUnit (long unitId) throws ServiceException;

    /** Returns details for the specified definition.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    DefDetail getDef (long defId) throws ServiceException;

    /** Returns the details for the specified type.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    TypeDetail getType (long defId) throws ServiceException;

    /** Returns a summary of the specified type.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    TypeSummary getSummary (long defId) throws ServiceException;

    /** Returns the contents of the specified definition.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    DefContent getContent (long defId) throws ServiceException;

    /** Returns the supertypes of the specified definition. The 2D array is of the form:
     * { ..., { grandparent, pextra1, pextra2 }, { parent, extra1, extra2 } }, where parent is the
     * primary supertype of the def, and grandparent is the primary supertype of parent. The
     * "extra" types are extra supertypes of the definition, and the "pextra" supertypes are extra
     * supertypes of the parent. Note that it is possible for the grandparentmost primary type to
     * be null, if the root has extra supertypes but no primary.
     * @throws ServiceException with e.no_such_def if the def is unknown. */
    Def[][] getSuperTypes (long defId) throws ServiceException;

    /** Searches for defs in the specified project that match the specified query. */
    DefDetail[] search (long projectId, String query) throws ServiceException;
}
