//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, PendingProject, Project => JProject}
import coreen.persist.{DB, Project}
import coreen.project.Importer
import coreen.rpc.LibraryService

/**
 * The implementation of library services provided by {@link LibraryService}.
 */
class LibraryServlet extends RemoteServiceServlet with LibraryService
{
  // from interface LibraryService
  def getProjects :Array[JProject] = {
    transaction {
      from(DB.projects) { p =>
        select(p)
//        orderBy(p.name)
      } map(Convert.toJava) toArray
    }
  }

  // from interface LibraryService
  def getPendingProjects :Array[PendingProject] = Importer.getPendingProjects

  // from interface LibraryService
  def  importProject (source :String) :PendingProject = Importer.importProject(source)
}
