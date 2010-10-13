//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, DefId, CompUnit, PendingProject, Project => JProject, Type}
import coreen.persist.{DB, Decode, Project, Def}
import coreen.project.Importer
import coreen.rpc.LibraryService

/** Provides the library servlet. */
trait LibraryServlet {
  this :DB with Importer =>

  /** The implementation of library services provided by {@link LibraryService}. */
  class LibraryServlet extends RemoteServiceServlet with LibraryService
  {
    // from interface LibraryService
    def getProjects :Array[JProject] = {
      transaction {
        from(_db.projects) { p =>
          select(p)
          //        orderBy(p.name)
        } map(Convert.toJava) toArray
      }
    }

    // from interface LibraryService
    def getPendingProjects :Array[PendingProject] = _importer.getPendingProjects

    // from interface LibraryService
    def importProject (source :String) :PendingProject = _importer.importProject(source)

    // from interface LibraryService
    def search (query :String) :Array[LibraryService.SearchResult] = transaction {
      val res = _db.resolveMatches(
        _db.defs.where(d => d.name === query and
                       d.typ.~ < Decode.typeToCode(Type.TERM)).toSeq,
        () => new LibraryService.SearchResult)
      // resolve the names of the projects from whence these results come
      val projIds = res map(_.unit.projectId) toSet
      val projMap = from(_db.projects)(p => where(p.id in projIds) select(p.id, p.name)) toMap;
      res foreach { r => r.project = projMap(r.unit.projectId) }
      res
    }
  }
}
