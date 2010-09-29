//
// $Id$

package coreen.project

import org.squeryl.PrimitiveTypeMode._

import coreen.persist.DB
import coreen.server.Services._

/**
 * A command-line tool for manipulating projects.
 */
object Tool extends Log with Dirs with Database
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
      DB.projects foreach { p =>
        println(p.id + " " + p.name)
      }
    }
  }

  def updateProject (pid :Long) {
    transaction {
      DB.projects.lookup(pid) match {
        case None => error("No project with id " + pid)
        case Some(p) => Updater.update(p, s => println(s))
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
