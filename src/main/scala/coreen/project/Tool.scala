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
      println("Usage: ptool { list | update pid }"); System.exit(255)
  }

  def listProjects {
    transaction {
      from(DB.projects) { p =>
        select(p)
//        orderBy(p.name)
      } foreach { p =>
        println(p.id + " " + p.name)
      }
    }
  }

  def updateProject (pid :Long) {
    println("TODO: updateProject " + pid)
  }

  protected def invoke (action : =>Unit) {
    initServices
    startServices
    action
    shutdownServices
  }
}
