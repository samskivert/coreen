//
// $Id$

package coreen.server

import javax.servlet.http.HttpServletResponse
import com.google.gwt.user.server.rpc.RemoteServiceServlet

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Convert, DefId, CompUnit, PendingProject, Project => JProject, Kind}
import coreen.persist.{DB, Decode, Project, Def}
import coreen.project.Importer
import coreen.rpc.{LibraryService, ServiceException}

/** Provides the library servlet. */
trait LibraryServlet {
  this :DB with Config with Importer =>

  /** The implementation of library services provided by {@link LibraryService}. */
  class LibraryServlet extends RemoteServiceServlet with LibraryService
  {
    // from interface LibraryService
    def getProjects = {
      transaction {
        from(_db.projects) { p =>
          select(p) orderBy(p.name)
        } map(Convert.toJava) toArray
      }
    }

    // from interface LibraryService
    def getPendingProjects = _importer.getPendingProjects

    // from interface LibraryService
    def importProject (source :String) = _importer.importProject(source)

    // from interface LibraryService
    def search (query :String) :Array[LibraryService.SearchResult] = transaction {
      if (query.trim.length == 0) Array()
      else {
        val res = _db.resolveMatches(_db.findDefs(query, Kind.FUNC),
                                     () => new LibraryService.SearchResult)
        // resolve the names of the projects from whence these results come
        val projIds = res map(_.unit.projectId) toSet
        val projMap = from(_db.projects)(p => where(p.id in projIds) select(p.id, p.name)) toMap;
        res foreach { r => r.project = projMap(r.unit.projectId) }
        res
      }
    }

    // from interface LibraryService
    def getConfig () = {
      val result = new java.util.HashMap[String,String]
      for ((k, v) <- _config.getSnapshot) {
        result.put(k, v)
      }
      result
    }

    // from interface LibraryService
    def updateConfig (key :String, value :String) {
      _config.update(key, value)
    }

    override protected def doUnexpectedFailure (e :Throwable) {
      e.printStackTrace
      getThreadLocalResponse.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage)
    }
  }
}
