//
// $Id$

package coreen.server

import java.io.File

import scala.io.Source
import scala.collection.mutable.ArrayBuffer

import com.google.gwt.user.server.rpc.RemoteServiceServlet
import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, Project => JProject, CompUnit => JCompUnit, Def => JDef}
import coreen.model.{CompUnitDetail, DefContent, DefId, DefDetail, Type, TypeDetail, TypeSummary}
import coreen.persist.{DB, Decode, Project, CompUnit, Def}
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
    def deleteProject (id :Long) :Unit = transaction {
      // TODO: redo all of this with foreign keys and let the database sort it out
      val defIds = from(_db.defs, _db.compunits)(
        (d, cu) => where(d.unitId === cu.id and cu.projectId === id) select(d.id))
      _db.uses.deleteWhere(u => u.referentId in defIds)
      _db.defmap.deleteWhere(dn => dn.id in defIds)
      _db.defs.deleteWhere(d => d.id in from(_db.compunits)(
        cu => where(cu.projectId === id) select(cu.id)))
      _db.compunits.deleteWhere(cu => cu.projectId === id)
      _db.projects.deleteWhere(p => p.id === id)
    }

    // from interface ProjectService
    def getCompUnits (projectId :Long) :Array[JCompUnit] = transaction {
      _db.compunits.where(cu => cu.projectId === projectId) map(Convert.toJava) toArray
    }

    // from interface ProjectService
    def getModsAndMembers (projectId :Long) :Array[Array[JDef]] = transaction {
      val mods = _db.loadModules(projectId) map(d => (d.id -> d)) toMap
      val members = _db.defs where(d => d.outerId in mods.keySet) toArray
      val modMems = members groupBy(_.outerId) map {
        case (id, dfs) => (mods(id) +: dfs.sortBy(_.name)) map(Convert.toJava)
      }
      modMems.toArray sortBy(_.head.name)
    }

    /** Returns all modules in the specified project. */
    def getModules (projectId :Long) :Array[JDef] = transaction {
      _db.loadModules(projectId).toArray sortBy(_.name) map(Convert.toJava)
    }

    // from interface ProjectService
    def getTypes (projectId :Long) :Array[JDef] = transaction {
      from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === projectId and cu.id === d.unitId and
              (d.typ === Decode.typeToCode(Type.TYPE)))
        select(d)
      ).toArray sortBy(_.name) map(Convert.toJava)
    }

    // from interface ProjectService
    def getMembers (defId :Long) :Array[JDef] = transaction {
      _db.defs.where(d => d.outerId === defId).toArray sortBy(_.name) map(Convert.toJava)
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
      val cmap = _db.defs.where(d => d.outerId === defId).toArray sortBy(_.name) map(
        Convert.toJava) groupBy(_.`type`)
      td.types = cmap.getOrElse(Type.TYPE, Array())
      td.funcs = cmap.getOrElse(Type.FUNC, Array())
      td.terms = cmap.getOrElse(Type.TERM, Array())
      td
    }

    // from interface ProjectService
    def getSummary (defId :Long) :TypeSummary = transaction {
      val ts = initDefDetail(defId, new TypeSummary)
      val mems = _db.defs.where(d => d.outerId === defId).toArray
      // load up all of the members defined in supertypes (TODO: filter members of Object/root?)
      def loadSupers (superId :Long, filterIds :Set[Long]) :Array[Def] = {
        _db.defs.lookup(superId) match {
          case Some(sd) if (!isRoot(sd)) => {
            println("Adding supers for " + sd.name)
            val smems = _db.defs.where(d => d.outerId === superId and
                                       not (d.id in filterIds)) toArray;
            smems ++ loadSupers(sd.superId, filterIds ++ smems.map(_.superId))
          }
          case _ => Array[Def]()
        }
      }
      val supers = loadSupers(ts.superId, mems map(_.superId) toSet)
      val cmap = (mems ++ supers) sortBy(_.name) map(Convert.toDefInfo) groupBy(_.`type`)
      ts.types = cmap.getOrElse(Type.TYPE, Array())
      ts.funcs = cmap.getOrElse(Type.FUNC, Array())
      ts.terms = cmap.getOrElse(Type.TERM, Array())
      ts
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
        val defs = _db.defs.where(d => d.outerId in parents).toSeq
        if (defs.isEmpty) defs
        else defs ++ loadDefs(defs.map(_.id) toSet)
      }
      dc.defs = (loadDefs(Set(defId)) :+ d).toArray sortBy(_.defStart) map(Convert.toJava)
      dc.defs foreach { _.start -= start }
      val defIds = dc.defs map(_.id) toSet
      val uses = _db.uses.where(u => u.ownerId in defIds).toArray
      dc.uses = uses sortBy(_.useStart) map(Convert.toJava)
      dc.uses foreach { _.start -= start }
      dc
    }

    // from interface ProjectService
    def getSuperTypes (defId :Long) :Array[Array[JDef]] = transaction {
      val d = requireDef(defId)
      val buf = ArrayBuffer[Array[JDef]]()
      def addSuperTypes (d :Def) {
        val sdefs = _db.supers.left(d).toList
        if (!sdefs.isEmpty) {
          val (pdef, darray) = sdefs.find(_.id == d.superId) match {
            case None => (None, (null :: sdefs map(Convert.toJava)) toArray)
            case Some(pd) =>
              (Some(pd), (pd :: sdefs.filterNot(_.id == pd.id)) map(Convert.toJava) toArray)
          }
          if (!pdef.isEmpty) addSuperTypes(pdef.get)
          buf += darray
        }
      }
      addSuperTypes(d)
      buf.toArray
    }

    // from interface ProjectService
    def search (projectId :Long, query :String) :Array[DefDetail] = transaction {
      _db.resolveMatches(from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === projectId and
              d.unitId === cu.id and
              d.name === query and
              d.typ.~ < Decode.typeToCode(Type.TERM))
        select(d)) toSeq, () => new DefDetail)
    }

    private def isRoot (df :Def) = df.name == "Object" // TODO!

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
      Convert.initDefInfo(d, dd)
      dd.unit = Convert.toJava(_db.compunits.lookup(d.unitId).get)
      dd.path = loadDefPath(d.outerId, Nil).toArray
      dd
    }

    private def loadDefPath (defId :Long, path :List[DefId]) :List[DefId] =
      if (defId == 0L) path else {
        val d = _db.defs.lookup(defId).get
        loadDefPath(d.outerId, Convert.toDefId(d) :: path)
      }

    private def initDefDetail[DD <: DefDetail] (defId :Long, dd :DD) :DD =
      initDefDetail(requireDef(defId), dd)

    private def loadCompUnitDetail (p :Project, unit :CompUnit) = {
      val detail = new CompUnitDetail
      detail.unit = Convert.toJava(unit)
      detail.text = loadSource(p, detail.unit)
      detail.defs = _db.defs.where(d => d.unitId === unit.id).toArray sortBy(_.defStart) map(
        Convert.toJava)
      detail.uses = _db.uses.where(u => u.unitId === unit.id).toArray sortBy(_.useStart) map(
        Convert.toJava)
      detail
    }

    private def loadSource (p :Project, unit :JCompUnit) =
      Source.fromURI(new File(p.rootPath).toURI.resolve(unit.path)).mkString("")

    private val LineSeparator = System.getProperty("line.separator")
  }
}
