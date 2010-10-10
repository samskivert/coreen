//
// $Id$

package coreen.server

import java.io.File

import scala.io.Source

import com.google.gwt.user.server.rpc.RemoteServiceServlet
import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, Project => JProject, CompUnit => JCompUnit, Def => JDef}
import coreen.model.{CompUnitDetail, DefContent, DefDetail, TypeDetail, TypedId}
import coreen.persist.{DB, Project, CompUnit, Def}
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
    def getModsAndMembers (projectId :Long) :Array[Array[JDef]] = transaction {
      val mods = from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === projectId and cu.id === d.unitId and
              (d.typ === _db.typeToCode(JDef.Type.MODULE)))
        select(d)
      ) map(d => (d.id -> d)) toMap

      val members = _db.defs where(d => d.parentId in mods.keySet) toArray
      val modMems = members groupBy(_.parentId) map {
        case (id, dfs) => (mods(id) +: dfs.sortBy(_.name)) map(Convert.toJava(_db.codeToType))
      }
      modMems.toArray sortBy(_.head.name)
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

    // from interface ProjectService
    def getContent (defId :Long) :DefContent = transaction {
      val d = requireDef(defId)
      val dc = initDefDetail(d, new DefContent)
      val p = requireProject(dc.unit.projectId)

      // load up the source text for this definition
      val text = loadSource(p, dc.unit)
      val start = text.lastIndexOf(LineSeparator, d.bodyStart)+1
      dc.text = text.substring(start, d.bodyEnd)

      // load up all defs and uses that are children of the def in question
      def loadDefs (parents :Set[Long]) :Seq[Def] = {
        val defs = _db.defs.where(d => d.parentId in parents).toSeq
        if (defs.isEmpty) defs
        else defs ++ loadDefs(defs.map(_.id) toSet)
      }
      dc.defs = (loadDefs(Set(defId)) :+ d).toArray sortBy(_.defStart) map(
        Convert.toJava(_db.codeToType))
      dc.defs foreach { _.start -= start }
      val defIds = dc.defs map(_.id) toSet
      val uses = _db.uses.where(u => u.ownerId in defIds).toArray
      dc.uses = uses sortBy(_.useStart) map(Convert.toJava)
      dc.uses foreach { _.start -= start }
      dc
    }

    private def requireProject (id :Long) = transaction {
      _db.projects.lookup(id) match {
        case Some(p) => p
        case None => throw new ServiceException("e.no_such_project")
      }
    }

    private def requireDef (id :Long) = transaction {
      _db.defs.lookup(id) match {
        case Some(d) => d
        case None => throw new ServiceException("e.no_such_def")
      }
    }

    private def initDefDetail[DD <: DefDetail] (d :Def, dd :DD) :DD = {
      dd.`def` = Convert.toJava(_db.codeToType)(d)
      dd.unit = Convert.toJava(_db.compunits.lookup(d.unitId).get)
      dd.path = loadDefPath(d.parentId, Nil).toArray
      dd.sig = d.sig.getOrElse(null)
      dd.doc = d.doc.getOrElse(null)
      dd
    }

    private def loadDefPath (defId :Long, path :List[TypedId]) :List[TypedId] =
      if (defId == 0L) path else {
        val d = _db.defs.lookup(defId).get
        loadDefPath(d.parentId, Convert.toTypedId(_db.codeToType)(d) :: path)
      }

    private def initDefDetail[DD <: DefDetail] (defId :Long, dd :DD) :DD =
      initDefDetail(requireDef(defId), dd)

    private def loadCompUnitDetail (p :Project, unit :CompUnit) = {
      val detail = new CompUnitDetail
      detail.unit = Convert.toJava(unit)
      detail.text = loadSource(p, detail.unit)
      detail.defs = _db.defs.where(d => d.unitId === unit.id).toArray sortBy(_.defStart) map(
        Convert.toJava(_db.codeToType))
      detail.uses = _db.uses.where(u => u.unitId === unit.id).toArray sortBy(_.useStart) map(
        Convert.toJava)
      detail
    }

    private def loadSource (p :Project, unit :JCompUnit) =
      Source.fromURI(new File(p.rootPath).toURI.resolve(unit.path)).mkString("")

    private val LineSeparator = System.getProperty("line.separator")
  }
}
