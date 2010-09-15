//
// $Id$

package coreen.ingress

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.regex.Pattern

import scala.collection.JavaConversions._

import coreen.model.PendingProject
import coreen.server.Main.log

/**
 * Handles the importation of projects into Coreen.
 */
object Importer
{
  /** Requests a snapshot of the currently pending projects. */
  def getPendingProjects :Array[PendingProject] = _projects.values.toArray sortBy(_.started)

  /** Requests that the project at the specified source be imported. */
  def importProject (source :String) :PendingProject = {
    val now = System.currentTimeMillis
    val pp = new PendingProject(source, "Starting...", now, now, false)
    _projects += (source -> pp)
    _executor.execute(new Runnable {
      override def run = processImport(source)
    })
    pp
  }

  /** Instructs the importer to shut down its executor (TODO: and any pending imports). */
  def shutdown {
    _executor.shutdown
  }

  private def updateProject (source :String, status :String, complete :Boolean) {
    _projects.get(source) match {
      case Some(pp) => {
        // this instance may be going out over the wire as this update happens, but even in the
        // event of such a race, nothing especially bad is going to happen
        pp.status = status
        pp.lastUpdated = System.currentTimeMillis
        pp.complete = complete
      }
      case None => {
        log.warning("Requested to update unknown project", "source", source, "status", status);
      }
    }
  }

  // methods from here down are invoked on a separate worker thread; be careful!
  private def processImport (source :String) {
    updateProject(source, "Identifying source...", false)

    // see if we can figure out where the data is
    val file = new File(source)
    if (file.isDirectory) {
      localProjectImport(source, file)
    } else if (file.exists && ARCHIVE_SUFFS.matcher(file.getName()).matches) {
      localArchiveImport(source, file)
    // } else if (GIT_URL.matches(sourcE)) {
    // } else if (SVN_URL.matches(sourcE)) {
    // } else if (HG_URL.matches(sourcE)) {
    } else {
      updateProject(source, "Unable to identify source", true)
    }
  }

  def localProjectImport (source :String, file :File) {
    updateProject(source, "TODO: local project import...", false)
  }

  def localArchiveImport (source :String, file :File) {
    updateProject(source, "TODO: local archive import...", false)
  }

  private val _projects :collection.mutable.Map[String,PendingProject] =
    new ConcurrentHashMap[String,PendingProject]()
  private val _executor = Executors.newFixedThreadPool(4) // TODO: configurable

  private val ARCHIVE_SUFFS = Pattern.compile("(.tgz|.tar.gz|.zip|.jar)$")
}
