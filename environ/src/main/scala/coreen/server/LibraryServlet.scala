//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, PendingProject, Project => JProject}
import coreen.persist.Repository
import coreen.persist.Project
import coreen.rpc.LibraryService

/**
 * The implementation of library services provided by {@link LibraryService}.
 */
class LibraryServlet extends RemoteServiceServlet with LibraryService
{
  // from interface LibraryService
  def getProjects :Array[JProject] = {
    transaction {
      from(Repository.projects) { p =>
        select(p)
//        orderBy(p.name)
      } map(Convert.toJava) toArray
    }
  }

  // from interface LibraryService
  def getPendingProjects :Array[PendingProject] = {
    null
  }

  // from interface LibraryService
  def  importProject (source :String) :PendingProject = {
    null
  }
}
