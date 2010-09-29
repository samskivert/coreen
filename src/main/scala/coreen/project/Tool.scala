//
// $Id$

package coreen.project

import org.squeryl.PrimitiveTypeMode._

import coreen.persist.{DB, DBService}
import coreen.server.{Dirs, DirsService}
import coreen.server.{Exec, ExecService}
import coreen.server.{Log, LogService}

/**
 * A command-line tool for manipulating projects.
 */
object Tool extends AnyRef
  with Log with LogService
  with Exec with ExecService
  with DB with DBService
  with Dirs with DirsService
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
    initServices
    startServices
    action
    shutdownServices
  }

  protected def error (msg :String) {
    println(msg)
    System.exit(255)
  }
}
