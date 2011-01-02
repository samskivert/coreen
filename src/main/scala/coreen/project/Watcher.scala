//
// $Id$

package coreen.project

import scala.collection.mutable.{Map => MMap}
import scala.actors.Actor

import java.io.File
import java.util.regex.Pattern

import org.squeryl.PrimitiveTypeMode._

import net.contentobjects.jnotify.{JNotify, JNotifyException, JNotifyListener}

import coreen.persist.{DB, Project}
import coreen.server.{Log, Exec, Component}

/** Provides watcher services. */
trait Watcher {
  trait WatcherService {
    /** Notifies the watcher that a new project has been updated (or created). */
    def projectUpdated (p :Project)

    /** Notifies the watcher that a project was deleted. */
    def projectDeleted (p :Project)
  }

  /** Provides watcher services. */
  val _watcher :WatcherService
}

/** Handles the lifecycle of the watcher. */
trait WatcherComponent extends Component with Watcher {
  this :Log with Exec with DB with Updater =>

  val _watcher = new WatcherService {
    def projectUpdated (p :Project) {
      handler ! UpdateWatch(p)
    }

    def projectDeleted (p :Project) {
      handler ! RemoveWatch(p)
    }
  }

  override protected def startComponents {
    super.startComponents
    // queue up adds for all known projects
    transaction {
      _db.projects foreach { p => handler ! AddWatch(p) }
    }
    // start the handler
    handler.start
  }

  override protected def shutdownComponents {
    super.shutdownComponents
    // shutdown the handler
    handler ! Shutdown()
  }

  case class AddWatch (p :Project)
  case class UpdateWatch (p :Project)
  case class RemoveWatch (p :Project)
  case class Shutdown ()

  // we handle all watcher activities on a single thread for safety
  val handler :Actor = new Actor {
    def act () { loopWhile(_running) { react {
      case AddWatch(p) => {
        _watches += (p.id -> new Watcher(p))
      }

      case UpdateWatch(p) => {
        // remove any old watcher (clearing out its watches) and add a new watcher
        _watches remove(p.id) map(_.shutdown)
        _watches += (p.id -> new Watcher(p))
      }

      case RemoveWatch(p) => _watches remove(p.id) match {
        case Some(w) => w.shutdown
        case None => _log.warning("Requested to remove unknown watch", "proj", p)
      }

      case Shutdown => _running = false
    }}}

    val _watches = MMap[Long,Watcher]()
    var _running = true
  }

  class Watcher (p :Project) extends JNotifyListener {
    // compute the set of directories that contain compilation units for this project and add
    // watches on each such directory individually (because recursive watches don't seem to work on
    // linux at least); TODO: use recursive watches on Windows?
    val ids :Set[Int] = transaction {
      val paths = from(_db.compunits)(cu => where(cu.projectId === p.id) select(cu.path))
      // we filter out the module comp unit which has empty path
      Set() ++ paths filterNot(_ == "") map(p => FilePart.matcher(p).replaceAll(""))
    } flatMap addWatch

    _log.info("Watching " + ids.size + " paths for " + p.name)

    /** Removes the watches handled by this watcher. */
    def shutdown {
      ids map removeWatch
    }

    // from interface JNotifyListener
    def fileCreated (id :Int, rootPath :String, name :String) {
      // TODO: if this is not a temporary file (by what criteria?) we should queue a rebuild
      _log.debug("File created", "id", id, "rootPath", rootPath, "name", name)
    }
    def fileDeleted (id :Int, rootPath :String, name :String) {
      // TODO: if the file is a compunit, we really need to rebuild the whole thing...
      _log.debug("File deleted", "id", id, "rootPath", rootPath, "name", name)
    }
    def fileModified (id :Int, rootPath :String, name :String) {
      _log.debug("File modified", "id", id, "rootPath", rootPath, "name", name)
      maybeQueueUpdate(rootPath, name)
    }
    def fileRenamed (id :Int, rootPath :String, oname :String, nname :String) {
      _log.debug("File renamed", "id", id, "rootPath", rootPath, "oname", oname, "nname", nname)
      // svn (at least) updates projects by renaming temporary files over the top of existing
      // project files, so we catch that rename and trigger an update
      maybeQueueUpdate(rootPath, nname)
      // TODO: if the old file is a compunit, we may need to rebuild the whole thing...
    }

    private def addWatch (path :String) = try {
      Some(JNotify.addWatch(p.rootPath + FS + path, JNotify.FILE_ANY, false, this))
    } catch {
      case e :JNotifyException =>
        _log.warning("Error adding watch", "path", path, "msg", e.getMessage,
                     "code", e.getErrorCode, "syscode", e.getSystemError)
      None
    }

    private def removeWatch (id :Int) = try {
      if (!JNotify.removeWatch(id)) {
        _log.warning("Error removing watch", "id", id)
      }
    } catch {
      case e :JNotifyException =>
        _log.warning("Error removing watch", "id", id, "msg", e.getMessage,
                     "code", e.getErrorCode, "syscode", e.getSystemError)
    }

    // if this is a compilation unit in a project, queue it for update
    private def maybeQueueUpdate (rootPath :String, name :String) {
      val abspath = new java.io.File(new java.io.File(rootPath), name).getCanonicalPath
      if (!abspath.startsWith(p.rootPath)) {
        _log.warning("Notified of update outside project directory?", "proj", p.name,
                     "proot", p.rootPath, "upath", rootPath, "uname", name)
      } else {
        val relpath = abspath.substring(p.rootPath.length+1)
        val cus = transaction { _db.compunits.where(cu => cu.path === relpath).toSet }
        if (!cus.isEmpty) queueUpdate(p)
        else _log.debug("Ignoring updated file", "proj", p.name, "path", relpath)
      }
    }

    private def queueUpdate (p :Project) {
      // TODO: be smarter about ensuring that a project isn't repeatedly queued for update
      if (!_updater.isUpdating(p)) {
        // load a fresh copy of the project from the database before sending it to the updater,
        // because we may be holding on to a stale copy
        val lp = transaction { _db.projects.lookup(p.id).get }
        _exec.executeJob("File modification triggered rebuild: " + lp.name,
                         () => _updater.update(lp, false))
      }
    }
  }

  val FS = File.separator
  val FilePart = Pattern.compile(FS + "[^" + FS + "]*$") // /[^/]*$
}

trait NoopWatcherComponent extends Component with Watcher {
  val _watcher = new WatcherService {
    def projectUpdated (p :Project) { /* nada! */ }
    def projectDeleted (p :Project) { /* nada! */ }
  }
}
