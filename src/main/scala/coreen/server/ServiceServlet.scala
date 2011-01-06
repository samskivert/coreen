//
// $Id$

package coreen.server

import java.io.File
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.squeryl.PrimitiveTypeMode._

import coreen.model.Kind
import coreen.persist.{DB, CompUnit, Decode, Def, Project}

/** Provides the service servlet. */
trait ServiceServlet {
  this :Log with DB =>

  /**
   * Provides code metadata to external entities like editors.
   */
  class ServiceServlet extends HttpServlet
  {
    override def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
      try {
        requireParameter(req, "action") match {
          case action @ ("resolve" | "view") => handleResolve(
            action, rsp, requireParameter(req, "src"),
            requireParameter(req, "pos").toInt, requireParameter(req, "sym"))
          case action => throw new Exception("unknown action " + action)
        }
      } catch {
        case e => {
          _log.warning("Service request failure", "req", req.getRequestURL, e)
          rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage)
        }
      }
    }

    private def handleResolve (action: String, rsp :HttpServletResponse,
                               src :String, pos :Int, sym :String) {
      val sfile = new File(src)
      if (!sfile.exists) throw new Exception("No such file " + src)
      val spath = sfile.getCanonicalPath

      val matches = transaction {
        // find the defs that overlap the supplied source position
        val odefs = for {
          p <- _db.projects; if (spath.startsWith(p.rootPath + File.separator));
          // find the project and compunit that match the supplied path
          val unitPath = spath.substring(p.rootPath.length+1);
          unit <- _db.compunits.where(cu => cu.path === unitPath);
          // find the defs in this unit that overlap the pos
          odef <- _db.defs.where(d => (d.unitId === unit.id) and
                                 (d.bodyStart lte pos) and (d.bodyEnd gt pos))
        } yield odef

        // if any of the overlapping defs exactly matches the position...
        odefs.find(d => d.defStart <= pos && d.defEnd > pos) match {
          // ...yield that def rather than looking for an overlapping use
          case Some(edef) => resolveDef(edef).toList
          // otherwise yield the uses enclosed in the overlapping defs that themselves overlap
          // the source position
          case _ => for {
            use <- _db.uses.where(u => (u.ownerId in odefs.map(_.id).toSet) and
                                  (u.useStart lte pos) and (u.useEnd gt pos));
            tgt <- _db.defs.lookup(use.referentId)
            result <- resolveDef(tgt)
          } yield result
        }
      }

      if ("resolve" == action) {
        // if we have matches, respond with them
        if (!matches.isEmpty) respond(rsp, "match", matches)
        // otherwise do an inexact search on the supplied symbol and return those matches
        else {
          val loose = transaction { _db.findDefs(sym, Kind.FUNC) flatMap(resolveDef) }
          if (!loose.isEmpty) respond(rsp, "match", loose)
          else respond(rsp, List("nomatch"))
        }

      } else { // "view"
        // if we have matches, redirect them to a page displaying the first matching def
        if (!matches.isEmpty) {
          val (proj, unit, tgt) = matches.head
          val det = Decode.codeToKind(tgt.kind) match {
            case Kind.MODULE => "MDS"
            case Kind.TYPE => "TYP"
            case _ => "DEF"
          }
          rsp.sendRedirect("/coreen/#PROJECT~" + proj.id + "~" + det + "~" + tgt.id)
        }
        // otherwise redirect them to a search on the sought symbol name
        else rsp.sendRedirect("/coreen/#LIBRARY~search~" + sym)
      }
    }

    /** Resolves a Def into Option[(Project, CompUnit, Def)]. */
    private def resolveDef (tgt :Def) :Option[(Project, CompUnit, Def)] = for {
      unit <- _db.compunits.lookup(tgt.unitId);
      proj <- _db.projects.lookup(unit.projectId)
    } yield (proj, unit, tgt)

    /** Writes response matches to the requester. */
    private def respond (rsp :HttpServletResponse, mtype :String,
                         results :Iterable[(Project, CompUnit, Def)]) {
      respond(rsp, for ((proj, unit, tgt) <- results) yield
        mtype + " " + proj.rootPath + File.separator + unit.path + " " + tgt.defStart)
    }

    /** Writes a response to the requester. */
    private def respond (rsp :HttpServletResponse, results :Iterable[String]) {
      val out = rsp.getWriter
      results.foreach { out.println(_) }
      out.close
    }

    /** Returns the value for the specified parameter, excepting if it is absent. */
    private def requireParameter (req :HttpServletRequest, name :String) :String =
      Option(req.getParameter(name)).getOrElse(
        throw new Exception("Missing parameter '" + name + "'"))
  }
}
