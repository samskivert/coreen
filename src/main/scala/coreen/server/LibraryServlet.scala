//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, PendingProject, Project => JProject}
import coreen.persist.{DB, Project}
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
  }
}
