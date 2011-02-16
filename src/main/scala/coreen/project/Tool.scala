//
// $Id$

package coreen.project

import org.squeryl.PrimitiveTypeMode._

import coreen.model.{Kind, SourceModel}
import coreen.persist.{DBComponent, Decode}
import coreen.server.{DirsComponent, ExecComponent, LogComponent, StdoutConsoleComponent}

/**
 * A command-line tool for manipulating projects.
 */
object Tool extends AnyRef
  with LogComponent with DirsComponent with ExecComponent with DBComponent
  with StdoutConsoleComponent with NoopWatcherComponent
  with Updater with Importer with SourceModel
{
  def main (args :Array[String]) :Unit = try {
    args match {
      case Array("list") => invoke(listProjects)
      case Array("update", pid) => invoke(updateProject(pid.toInt))
      case Array("import", dir) => invoke(importProject(dir))
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
        case Some(p) => _updater.update(p)
      }
    }
  }

  def importProject (dir :String) {
    _importer.importProject(dir)
    Thread.sleep(3000L) // give the async tasks a moment to get queued up
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
