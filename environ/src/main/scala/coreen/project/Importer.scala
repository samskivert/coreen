//
// $Id$

package coreen.project

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.regex.Pattern

import org.squeryl.PrimitiveTypeMode._
import scala.collection.JavaConversions._

import coreen.model.PendingProject
import coreen.persist.{DB, Project}
import coreen.rpc.ServiceException
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
    // make sure we're not already importing a project at this source
    if (_projects.keySet(source)) {
      throw new ServiceException("Already importing a project with source '" + source + "'")
    }
    val now = System.currentTimeMillis
    val pp = new PendingProject(source, "Starting...", now, now, false)
    _projects += (source -> pp)
    _executor.execute(new Runnable {
      override def run = {
        try {
          processImport(source)
        } catch {
          case e => updatePending(source, e.getMessage, true)
        }
      }
    })
    pp
  }

  /** Instructs the importer to shut down its executor (TODO: and any pending imports). */
  def shutdown {
    _executor.shutdown
  }

  // this method is called from all sorts of threads and is (mostly) safe
  private def updatePending (source :String, status :String, complete :Boolean) {
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
    updatePending(source, "Identifying source...", false)

    // see if we can figure out where the data is
    (source, new File(source)) match {
      case (_, f) if (f.isDirectory) => localProjectImport(source, f)
      case (_, f) if (f.exists && ArchiveSuffsRE.matcher(f.getName()).matches) =>
        localArchiveImport(source, f)
      // case GitRepoRE => gitRepoImport(source)
      // case SvnRepoRE => hgRepoImport(source)/
      // case URLRE => see what's on the other end of the URL...
      case _ => updatePending(source, "Unable to identify source", true)
    }
  }
  private[project] val ArchiveSuffsRE = Pattern.compile("(.tgz|.tar.gz|.zip|.jar)$")
  // private[project] val GitRepoRE = """https?://.*\.git""".r
  // private[project] val SvnRepoRE = """svn://.*""".r


  private def localProjectImport (source :String, file :File) {
    // try deducing the name and version from the project directory name
    updatePending(source, "Inferring project name and version...", false)
    val (name, vers) = inferNameAndVersion(file.getName)

    // create the project metadata
    val p = createProject(source, name, file, "0.0")

    // TODO: find all the compilation units and process them
    updatePending(source, "Inferring project name and version...", false)

    // finally, report that the import is complete
    updatePending(source, "Import complete.", true)
  }

  private def localArchiveImport (source :String, file :File) {
    updatePending(source, "TODO: local archive import...", false)
  }

  private[project] def createProject (
    source :String, name :String, rootPath :File, version :String) {
    val now = System.currentTimeMillis
    DB.projects.insert(new Project(name, rootPath.getAbsolutePath, version, now, now))
  }

  /** Attempts to extract a name and version from file or directory names like:
   * foo-1.0, foo-r25, foo-1.0beta, foo-bar-1.0, foo_1.0, etc. */
  private[project] def inferNameAndVersion (name :String) = name match {
    case NameVersionRE(name, vers) => (name, vers)
    case _ => (name, "1.0")
  }
  private[project] val NameVersionRE = """(.*)[_-](r?[0-9]+.*)""".r

  private val _projects :collection.mutable.Map[String,PendingProject] =
    new ConcurrentHashMap[String,PendingProject]()
  private val _executor = Executors.newFixedThreadPool(4) // TODO: configurable
}
