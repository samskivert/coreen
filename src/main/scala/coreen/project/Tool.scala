//
// $Id$

package coreen.project

import coreen.persist.DBModule
import coreen.server.{DirsModule, ExecutorModule, LogModule}
import coreen.server.{HttpServerModule, ProjectServletModule, LibraryServletModule}

import org.squeryl.PrimitiveTypeMode._

import coreen.server.Services._

/**
 * A command-line tool for manipulating projects.
 */
object Tool extends LogModule with ExecutorModule with HttpServerModule with DBModule
               with DirsModule with UpdaterModule with ImporterModule with ProjectServletModule
               with LibraryServletModule
               with Log with Dirs with Database with Executor
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
