//
// $Id$

package coreen.project

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

import org.squeryl.PrimitiveTypeMode._
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

import coreen.model.PendingProject
import coreen.persist.{DB, Project}
import coreen.rpc.ServiceException
import coreen.server.{Log, Exec}

/** Provides project importing services. */
trait Importer {
  this :Log with Exec with DB with Updater =>

  /** Handles the importation of projects into Coreen. */
  object _importer {
    /** Requests a snapshot of the currently pending projects. */
    def getPendingProjects :Array[PendingProject] = _projects.values.toArray sortBy(_.started)

    /** Requests that the project at the specified source be imported. */
    def importProject (source :String) :PendingProject = {
      // make sure we're not already importing a project at this source
      if (_projects.keySet(source)) {
        throw new ServiceException("Already importing a project with source '" + source + "'")
      }
      val now = System.currentTimeMillis
      val pp = new PendingProject(source, "Starting...", now, now, 0L)
      _projects += (source -> pp)
      _exec.execute(new Runnable {
        override def run = {
          try {
            processImport(source)
          } catch {
            case t => t.printStackTrace; updatePending(source, "Error: " + t.getMessage, -1L)
          }
        }
      })
      pp
    }

    // this method is called from all sorts of threads and is (mostly) safe
    private def updatePending (source :String, status :String, projectId :Long) {
      _projects.get(source) match {
        case Some(pp) => {
          // this instance may be going out over the wire as this update happens, but even in the
          // event of such a race, nothing especially bad is going to happen
          pp.status = status
          pp.lastUpdated = System.currentTimeMillis
          pp.projectId = projectId
        }
        case None => {
          _log.warning("Requested to update unknown project", "source", source, "status", status);
        }
      }
    }

    // methods from here down are invoked on a separate worker thread; be careful!
    private def processImport (source :String) {
      updatePending(source, "Identifying source...", 0L)

      // see if we can figure out where the data is
      (source, new File(source)) match {
        case (_, f) if (f.isDirectory) => localProjectImport(source, f)
        case (_, f) if (f.exists && ArchSuffsRE.matcher(f.getName()).matches) =>
          localArchiveImport(source, f)
        // case GitRepoRE => gitRepoImport(source)
        // case SvnRepoRE => hgRepoImport(source)/
        // case URLRE => see what's on the other end of the URL...
        case _ => updatePending(source, "Unable to identify source", -1L)
      }
    }

    private def localProjectImport (source :String, file :File) {
      // try deducing the name and version from the project directory name
      updatePending(source, "Inferring project name and metadata...", 0L)
      val (name, vers) = inferNameAndVersion(file.getName)

      // create the project metadata
      val p = createProject(source, name, file, "0.0", inferSourceDirs(file))

      // "update" the project for the first time
      _updater.update(p)

      // report that the import is complete
      updatePending(source, "Import complete.", p.id)
    }

    private def localArchiveImport (source :String, file :File) {
      updatePending(source, "TODO: local archive import...", 0L)
    }

    private def createProject (
      source :String, name :String, rootPath :File, version :String, srcDirs :Option[String]) = {
      val now = System.currentTimeMillis
      transaction {
        _db.projects.insert(Project(name, rootPath.getAbsolutePath, version, srcDirs, now, now))
      }
    }

    /** Attempts to extract a name and version from file or directory names like:
     * foo-1.0, foo-r25, foo-1.0beta, foo-bar-1.0, foo_1.0, etc. */
    private def inferNameAndVersion (name :String) = name match {
      case NameVersionRE(name, vers) => (name, vers)
      case _ => (name, "1.0")
    }

    private def inferSourceDirs (root :File) = {
      val paths = ArrayBuffer[String]()
      // adds the first file in the list that exists (if any) to the paths
      def addFirstExister (files :List[File]) = files find(_.exists) foreach(
        f => paths += f.getAbsolutePath.substring(root.getAbsolutePath.length+1))

      // java project layouts
      addFirstExister(List(mkFile(root, "src", "main", "java"),
                           mkFile(root, "src", "java")))
      // TODO: other common project layouts?

      // if all else fails, try 'src' directory
      if (paths.length == 0) addFirstExister(List(mkFile(root, "src")))

      if (paths.isEmpty) None else Some(paths.mkString(" "))
    }

    private def mkFile (root :File, path :String*) = (root /: path)(new File(_, _))

    private val _projects :collection.mutable.Map[String,PendingProject] =
      new ConcurrentHashMap[String,PendingProject]()

    private[project] val NameVersionRE = """(.*)[_-](r?[0-9]+.*)""".r
    private[project] val ArchSuffsRE = Pattern.compile("(.tgz|.tar.gz|.zip|.jar)$")
    // private[project] val GitRepoRE = """https?://.*\.git""".r
    // private[project] val SvnRepoRE = """svn://.*""".r
  }
}
