//
// $Id$

package coreen.server

import java.io.File

import scala.io.Source

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, Project => JProject, CompUnit => JCompUnit, CompUnitDetail}
import coreen.persist.{DB, Project, CompUnit}
import coreen.project.Updater
import coreen.rpc.{ProjectService, ServiceException}

/** Provides the project servlet. */
trait ProjectServlet {
  this :Log with DB with Exec with Updater =>

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
      _exec.execute(new Runnable {
        override def run = {
          try {
            _updater.update(p, println)
          } catch {
            case t => _log.warning("Update failed", "proj", p, t)
          }
        }
      })
    }

    // from interface ProjectService
    def getCompUnits (projectId :Long) :Array[JCompUnit] = transaction {
      _db.compunits.where(cu => cu.projectId === projectId) map(Convert.toJava) toArray
    }

    // from interface ProjectService
    def getCompUnit (unitId :Long) :CompUnitDetail = transaction {
      _db.compunits.lookup(unitId) match {
        case Some(unit) => loadCompUnitDetail(requireProject(unit.projectId), unit)
        case None => throw new ServiceException("e.no_such_unit")
      }
    }

    private def requireProject (id :Long) = transaction {
      _db.projects.lookup(id) match {
        case Some(p) => p
        case None => throw new ServiceException("e.no_such_project")
      }
    }

    private def loadCompUnitDetail (p :Project, unit :CompUnit) = {
      val detail = new CompUnitDetail
      detail.unit = Convert.toJava(unit)
      detail.text = Source.fromURI(new File(p.rootPath).toURI.resolve(unit.path)).getLines.toArray
      detail.defs = _db.defs.where(d => d.unitId === unit.id).toArray sortBy(_.defStart) map(
        Convert.toJava(_db.codeToType))
      detail
    }
  }
}
