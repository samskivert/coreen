//
// $Id$

package coreen.project

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Def => JDef}
import coreen.persist.DBComponent
import coreen.server.{DirsComponent, ExecComponent, LogComponent}

/**
 * A command-line tool for manipulating projects.
 */
object Tool extends AnyRef
  with LogComponent with DirsComponent with ExecComponent with DBComponent
  with Updater with Importer
{
  def main (args :Array[String]) :Unit = try {
    args match {
      case Array("list") => invoke(listProjects)
      case Array("update", pid) => invoke(updateProject(pid.toInt))
      case Array("import", dir) => invoke(importProject(dir))
      case Array("types", pid) => invoke(dumpTypes(pid.toInt))
    }
  } catch {
    case _ :MatchError | _ :NumberFormatException =>
      error("Usage: ptool { list | update pid | import dir | types pid }")
  }

  def listProjects {
    transaction {
      _db.projects foreach { p =>
        println(p.id + " " + p.name)
      }
    }
  }

  def updateProject (pid :Long) {
    transaction {
      _db.projects.lookup(pid) match {
        case None => error("No project with id " + pid)
        case Some(p) => _updater.update(p, s => println(s))
      }
    }
  }

  def importProject (dir :String) {
    _importer.importProject(dir)
    Thread.sleep(3000L) // give the async tasks a moment to get queued up
  }

  def dumpTypes (pid :Long) {
    transaction {
      from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === pid and cu.id === d.unitId and
              (d.typ lte _db.typeToCode(JDef.Type.TYPE)))
        select(d)
      ) foreach { d => println(d.typ + " " + d.sig) }
    }
  }

  protected def invoke (action : =>Unit) {
    initComponents
    startComponents
    action
    shutdownComponents
  }

  protected def error (msg :String) {
    println(msg)
    System.exit(255)
  }
}
