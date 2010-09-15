//
// $Id$

package coreen.ingress

import java.util.concurrent.ConcurrentHashMap

import scala.collection.JavaConversions._

import coreen.model.PendingProject
import coreen.server.Main.log

/**
 * Handles the importation of projects into Coreen.
 */
object Importer
{
  /** Requests a snapshot of the currently pending projects. */
  def getPendingProjects :Array[PendingProject] = _projects.values map(_._1) toArray

  /** Requests that the project at the specified source be imported. */
  def importProject (source :String) :PendingProject = {
    val now = System.currentTimeMillis
    val pp = new PendingProject(source, "Starting...", now, now, false)
    _projects += (source -> (pp, new Importer(source)))
    pp
  }

  private def updateProject (source :String, status :String) {
    _projects.get(source) match {
      case (pp, _) => {
        // this instance may be going out over the wire as this update happens, but even in the
        // event of such a race, nothing especially bad is going to happen
        pp.status = status
        pp.lastUpdated = System.currentTimeMillis
      }
      case _ => {
        log.warning("Requested to update unknown project", "source", source, "status", status);
      }
    }
  }

  private val _projects = new ConcurrentHashMap[String,(PendingProject,Importer)]()
}

private class Importer (source :String) extends Thread
{
  import Importer._

  /* ctor */ {
    start
  }

  override def run {
    updateProject(source, "Identifying source...")

    // TODO: see if the source looks like a URL, or a directory, or a file on the filesystem, etc.
  }
}
