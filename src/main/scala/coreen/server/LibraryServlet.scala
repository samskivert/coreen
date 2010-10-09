//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, PendingProject, Project => JProject, TypedId}
import coreen.persist.{DB, Project, Def}
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
      val matches = _db.defs.where(d => d.name === query) toArray
      val unitMap = from(_db.compunits)(cu =>
        where(cu.id in matches.map(_.unitId).toSet) select(cu.id, cu.projectId)) toMap
      val projMap = from(_db.projects)(p =>
        where(p.id in unitMap.values) select(p.id, p.name)) toMap

      def mapped (defs :Seq[Def]) = defs map(m => (m.id -> m)) toMap
      def parents (defs :Seq[Def]) =  defs map(_.parentId) filter(0.!=) toSet
      def resolveDefs (have :Map[Long, Def], want :Set[Long]) :Map[Long, Def] = {
        val need = want -- have.keySet
        if (need.isEmpty) have
        else {
          val more = _db.defs.where(d => d.id in need).toArray
          resolveDefs(have ++ mapped(more), parents(more))
        }
      }

      val defMap = resolveDefs(mapped(matches), parents(matches))
      def mkPath (d :Option[Def], path :List[TypedId]) :Array[TypedId] = d match {
        case None => path.toArray
        case Some(d) => mkPath(defMap.get(d.parentId), Convert.toTypedId(_db.codeToType)(d) :: path)
      }

      matches map { d =>
        val pid = unitMap(d.unitId)
        new LibraryService.SearchResult(pid, projMap(pid), mkPath(defMap.get(d.parentId), List()),
                                        Convert.toJava(_db.codeToType)(d), d.doc.getOrElse(null))
      }
    }
  }
}
