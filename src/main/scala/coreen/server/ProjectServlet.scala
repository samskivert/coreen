//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, Project => JProject, CompUnit => JCompUnit}
import coreen.persist.DB
import coreen.project.Updater
import coreen.rpc.{ProjectService, ServiceException}

/**
 * The implementation of project services provided by {@link ProjectService}.
 */
class ProjectServlet extends RemoteServiceServlet with ProjectService
{
  // from interface ProjectService
  def getProject (id :Long) :JProject = Convert.toJava(requireProject(id))

  // from interface ProjectService
  def updateProject (id :Long) {
    val p = requireProject(id)
    Main.exec.execute(new Runnable {
      override def run = {
        try {
          Updater.update(p, println)
        } catch {
          case t => Main.log.warning("Update failed", "proj", p, t)
        }
      }
    })
  }

  // from interface ProjectService
  def getCompUnits (projectId :Long) :Array[JCompUnit] = {
    transaction {
      DB.compunits.where(cu => cu.projectId === projectId) map(Convert.toJava) toArray
    }
  }

  private[server] def requireProject (id :Long) = transaction {
    DB.projects.lookup(id) match {
      case Some(p) => p
      case None => throw new ServiceException("e.no_such_project")
    }
  }
}
