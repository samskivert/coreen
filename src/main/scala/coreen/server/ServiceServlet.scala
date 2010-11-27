//
// $Id$

package coreen.server

import java.io.File
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import org.squeryl.PrimitiveTypeMode._

import coreen.model.Kind
import coreen.persist.{DB, Decode, Project}

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
        val action = requireParameter(req, "action")

        if ("resolve" == action || "view" == action) {
          val src = requireParameter(req, "src")
          val pos = requireParameter(req, "pos").toInt

          val sfile = new File(src)
          if (!sfile.exists) throw new Exception("No such file " + src)
          val spath = sfile.getCanonicalPath

          val matches = transaction {
            for (p <- _db.projects; if (spath.startsWith(p.rootPath + File.separator));
                 // find the project and compunit that match the supplied path
                 val unitPath = spath.substring(p.rootPath.length+1);
                 unit <- _db.compunits.where(cu => cu.path === unitPath);
                 // find the defs in this unit that overlap the pos
                 val odefs = _db.defs.where(d =>
                   (d.unitId === unit.id) and (d.bodyStart lte pos) and (d.bodyEnd gt pos));
                 // find the uses enclosed in any of the above defs that overlap the pos
                 use <- _db.uses.where(u => (u.ownerId in odefs.map(_.id).toSet) and
                                            (u.useStart lte pos) and (u.useEnd gt pos));
                 // yield the target def, it's unit, and it's project
                 tgt <- _db.defs.lookup(use.referentId);
                 unit <- _db.compunits.lookup(tgt.unitId);
                 proj <- _db.projects.lookup(unit.projectId)) yield (proj, unit, tgt)
          }

          if ("resolve" == action) {
            val out = rsp.getWriter
            if (matches.isEmpty) {
              out.println("nomatch")
            } else for ((proj, unit, tgt) <- matches) {
              out.println("match " + proj.rootPath + File.separator +
                          unit.path + " " + tgt.defStart)
            }
            out.close

          } else { // "view"
            if (matches.isEmpty) {
              val sym = req.getParameter("sym")
              if (sym != null) {
                rsp.sendRedirect("/coreen/#LIBRARY~search~" + sym)
              } else {
                val out = rsp.getWriter
                out.println("Found no uses at " + pos + " in " + spath + ".")
                out.close
              }
            } else {
              val (proj, unit, tgt) = matches.head
              val det = Decode.codeToKind(tgt.kind) match {
                case Kind.MODULE => "MDS"
                case Kind.TYPE => "TYP"
                case _ => "DEF"
              }
              rsp.sendRedirect("/coreen/#PROJECT~" + proj.id + "~" + det + "~" + tgt.id)
            }
          }

        } else {
          throw new Exception("unknown action " + action)
        }

      } catch {
        case e => rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage)
      }
    }

    def requireParameter (req :HttpServletRequest, name :String) = {
      val value = req.getParameter(name)
      if (value == null) {
        throw new Exception("Missing parameter '" + name + "'")
      }
      value
    }
  }
}
