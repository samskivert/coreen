//
// $Id$

package coreen.project

import org.squeryl.PrimitiveTypeMode._

import coreen.persist.{DB, DBComponent}
import coreen.server.{Dirs, DirsComponent}
import coreen.server.{Exec, ExecComponent}
import coreen.server.{Log, LogComponent}

/**
 * A command-line tool for manipulating projects.
 */
object Tool extends AnyRef
  with Log with LogComponent
  with Exec with ExecComponent
  with DB with DBComponent
  with Dirs with DirsComponent
  with Updater with Importer
{
  def main (args :Array[String]) :Unit = try {
    args match {
      case Array("list") => invoke(listProjects)
      case Array("update", pid) => invoke(updateProject(pid.toInt))
    }
  } catch {
    case _ :MatchError | _ :NumberFormatException =>
      error("Usage: ptool { list | update pid }")
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
