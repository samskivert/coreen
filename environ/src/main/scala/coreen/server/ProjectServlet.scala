//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, Project => JProject}
import coreen.persist.DB
import coreen.rpc.ProjectService

/**
 * The implementation of project services provided by {@link ProjectService}.
 */
class ProjectServlet extends RemoteServiceServlet with ProjectService
{
  // from interface LibraryService
  def getProject (id :Long) :JProject = {
    transaction {
      DB.projects.lookup(id) match {
        case Some(p) => Convert.toJava(p)
        case None => null // oh the huge manatee
      }
    }
  }
}
