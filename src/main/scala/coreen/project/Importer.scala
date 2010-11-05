//
// $Id$

package coreen.project

import java.io.File
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.regex.Pattern

import org.squeryl.PrimitiveTypeMode._
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

import coreen.model.PendingProject
import coreen.persist.{DB, Project}
import coreen.rpc.ServiceException
import coreen.server.{Log, Dirs, Exec}
import coreen.util.RemoteUnpacker

/** Provides project importing services. */
trait Importer {
  this :Log with Dirs with Exec with DB with Updater =>

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
            case ie :ImportException => updatePending(source, ie.getMessage, -1L)
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
        case (_, f) if (f.isDirectory)      => localProjectImport(source, f)
        case (_, f) if (isLocalArchive(f))  => localArchiveImport(source, f)
        case _ if (isRemoteArchive(source)) => remoteArchiveImport(source)
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

      // finish up now that we have a name, version and local filesystem location
      finishLocalImport(source, file, name, vers);
    }

    private def isLocalArchive (file :File) =
      file.exists && ArchSuffsRE.matcher(file.getName()).matches

    private def localArchiveImport (source :String, file :File) {
      updatePending(source, "Inferring project name and metadata...", 0L)

      val suff = file.getName takeRight(4)
      if (suff != ".jar" && suff != ".zip")
        throw new ImportException("Only handle .zip and .jar archives currently.")
      val (name, vers) = inferNameAndVersion(file.getName dropRight(4))

      // prepare the project directory
      val pdir = _projectDir(name)
      if (pdir.exists && pdir.list.length > 0)
        throw new ImportException("Already have project in " + pdir)
      if (!pdir.mkdirs) throw new ImportException("Unable to create project directory: " + pdir)

      // unpack the source
      updatePending(source, "Unpacking source to local directory...", 0L)
      val unpacker = Runtime.getRuntime.exec(Array("jar", "xf", file.getAbsolutePath), null, pdir)
      val urv = unpacker.waitFor
      if (urv != 0) {
        updatePending(source, "Unpacking failed? Trying anyway...", 0L);
      }

      finishLocalImport(source, pdir, name, vers);
    }

    private def isRemoteArchive (source :String) = try {
      ArchSuffsRE.matcher(new URL(source).getPath).matches
    } catch {
      case e => false
    }

    private def remoteArchiveImport (source :String) {
      updatePending(source, "Inferring project name and metadata...", 0L)

      val url = new URL(source)
      val file = url.getPath.substring(url.getPath.lastIndexOf("/")+1)
      val (name, vers) = inferNameAndVersion(file)

      // prepare the project directory
      val pdir = _projectDir(name)
      if (pdir.exists && pdir.list.length > 0)
        throw new ImportException("Already have project in " + pdir)
      if (!pdir.mkdirs)
        throw new ImportException("Unable to create project directory: " + pdir)

      // download and unpack the source
      updatePending(source, "Downloading and unpacking source...", 0L)
      RemoteUnpacker.unpackJar(url, pdir)

      finishLocalImport(source, pdir, name, vers);
    }

    private def finishLocalImport (source :String, root :File, name :String, vers :String) {
      // create the project metadata
      val p = createProject(source, name, root, "0.0", inferSourceDirs(root), None)

      // report that the import is complete
      updatePending(source, "Project created. Processing contents...", p.id)

      // "update" the project for the first time
      _updater.update(p)
    }

    private def createProject (source :String, name :String, rootPath :File, version :String,
                               srcDirs :Option[String], readerOpts :Option[String]) = {
      val now = System.currentTimeMillis
      transaction {
        _db.projects.insert(Project(name, rootPath.getAbsolutePath, version,
                                    srcDirs, readerOpts, now, now))
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

    private class ImportException (msg :String) extends Exception(msg)

    private val _projects :collection.mutable.Map[String,PendingProject] =
      new ConcurrentHashMap[String,PendingProject]()

    private[project] val NameVersionRE = """(.*)[_-](r?[0-9]+.*)""".r
    private[project] val ArchSuffsRE = Pattern.compile(".*(.tgz|.tar.gz|.zip|.jar)$")
    // private[project] val GitRepoRE = """https?://.*\.git""".r
    // private[project] val SvnRepoRE = """svn://.*""".r
  }
}
