//
// $Id$

package coreen.server

import java.io.File
import scala.io.Source

import com.google.gwt.user.server.rpc.RemoteServiceServlet
import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, Project => JProject, CompUnit => JCompUnit, Def => JDef}
import coreen.model.{CompUnitDetail, DefDetail, TypeDetail}
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
    def getTypes (projectId :Long) :Array[JDef] = transaction {
      from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === projectId and cu.id === d.unitId and
              (d.typ === _db.typeToCode(JDef.Type.TYPE)))
        select(d)
      ).toArray sortBy(_.name) map(Convert.toJava(_db.codeToType))
    }

    // from interface ProjectService
    def getMembers (defId :Long) :Array[JDef] = transaction {
      _db.defs.where(d => d.parentId === defId).toArray sortBy(_.name) map(
        Convert.toJava(_db.codeToType))
    }

    // from interface ProjectService
    def getCompUnit (unitId :Long) :CompUnitDetail = transaction {
      _db.compunits.lookup(unitId) map { unit =>
        loadCompUnitDetail(requireProject(unit.projectId), unit)
      } getOrElse(throw new ServiceException("e.no_such_unit"))
    }

    // from interface ProjectService
    def getDef (defId :Long) :DefDetail = transaction {
      initDefDetail(defId, new DefDetail)
    }

    // from interface ProjectService
    def getType (defId :Long) :TypeDetail = transaction {
      val td = initDefDetail(defId, new TypeDetail)
      val cmap = _db.defs.where(d => d.parentId === defId).toArray sortBy(_.name) map(
        Convert.toJava(_db.codeToType)) groupBy(_.`type`)
      td.types = cmap.getOrElse(JDef.Type.TYPE, Array())
      td.funcs = cmap.getOrElse(JDef.Type.FUNC, Array())
      td.terms = cmap.getOrElse(JDef.Type.TERM, Array())
      td
    }

    private def requireProject (id :Long) = transaction {
      _db.projects.lookup(id) match {
        case Some(p) => p
        case None => throw new ServiceException("e.no_such_project")
      }
    }

    private def initDefDetail[DD <: DefDetail] (defId :Long, dd :DD) = {
      _db.defs.lookup(defId) map { d =>
        dd.`def` = Convert.toJava(_db.codeToType)(d)
        dd.projectId = _db.compunits.lookup(d.unitId).get.projectId
        dd.unitId = d.unitId
        dd.sig = d.sig.getOrElse(null)
        dd.doc = d.doc.getOrElse(null)
        dd
      } getOrElse(throw new ServiceException("e.no_such_def"))
    }

    private def loadCompUnitDetail (p :Project, unit :CompUnit) = {
      val detail = new CompUnitDetail
      detail.unit = Convert.toJava(unit)
      detail.text = Source.fromURI(new File(p.rootPath).toURI.resolve(unit.path)).mkString("")
      detail.defs = _db.defs.where(d => d.unitId === unit.id).toArray sortBy(_.defStart) map(
        Convert.toJava(_db.codeToType))
      detail.uses = _db.uses.where(u => u.unitId === unit.id).toArray sortBy(_.useStart) map(
        Convert.toJava)
      detail
    }
  }
}
