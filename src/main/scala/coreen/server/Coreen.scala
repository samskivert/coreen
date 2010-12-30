//
// $Id$

package coreen.server

import java.awt.Desktop
import java.io.{File, PrintStream, FileOutputStream, IOException}
import java.net.URI

import sun.misc.{Signal, SignalHandler}

import org.squeryl.PrimitiveTypeMode._

import coreen.persist.{DB, DBComponent}
import coreen.project.{Importer, Updater, WatcherComponent}

/**
 * The main entry point for the Coreen server.
 */
object Coreen extends AnyRef
  with LogComponent with DirsComponent with ExecComponent with DBComponent
  with ConfigComponent with TrayComponent with ConsoleComponent with WatcherComponent
  with HttpComponent with ProjectServlet with LibraryServlet with ConsoleServlet with ServiceServlet
  with Updater with Importer
{
  def main (args :Array[String]) {
    // if we're running via Getdown, redirect our log output to a file
    _appdir map { appdir =>
      // first delete any previous previous log file
      val olog = new File(appdir, "old-coreen.log")
      if (olog.exists) olog.delete

      // next rename the previous log file
      val nlog = new File(appdir, "coreen.log")
      if (nlog.exists) nlog.renameTo(olog)

      // and now redirect our output
      try {
        val logOut = new PrintStream(new FileOutputStream(nlog), true)
        System.setOut(logOut)
        System.setErr(logOut)
      } catch {
        case ioe :IOException =>
          _log.warning("Failed to open debug log", "path", nlog, "error", ioe)
      }
    }

    initComponents // initialize our components

    // register a signal handler to shutdown gracefully on ctrl-c
    var ohandler :SignalHandler = null
    ohandler = Signal.handle(_sigint, new SignalHandler {
      def handle (sig :Signal) {
        shutdown
      }
    })

    try {
      startComponents // start our components
    } catch {
      case e => _log.warning("Failure starting up", e); System.exit(255)
    }

    // if we're running in app mode, open a web browser
    if (_appdir.isDefined) {
      try {
        Desktop.getDesktop.browse(getServerURL("").toURI)
      } catch {
        case e => _log.warning("Unable to launch browser: " + e)
      }
    }

    _log.info("Coreen running. Ctrl-c to exit.")

    // queue any projects that have not been updated in this epoch
    if (_config.get("epoch", 0) != _epoch) {
      _config.update("epoch", _epoch)
      _config.update("epochStamp", System.currentTimeMillis())
    }
    val epochStamp = _config.get("epochStamp", 0L)
    transaction {
      for (p <- _db.projects) {
        if (p.lastUpdated < epochStamp) {
          _exec.queueJob("Epoch triggered rebuild: " + p.name, () => _updater.update(p))
        }
      }
    }

    // block the main thread until our signal is received
    _sigint.synchronized { _sigint.wait }

    _log.info("Coreen exiting...")
    Signal.handle(_sigint, ohandler) // restore old signal handler
    shutdownComponents // shutdown our components
  }

  def shutdown {
    _sigint.synchronized { _sigint.notify } // notify the main thread that it's OK to exit
  }

  // from trait Database
  // override protected def dblogger = (s :String) => println(s)

  private val _sigint = new Signal("INT")

  /** Increment this value to trigger a (one at a time) rebuild of all projects the next time
   * Coreen is started on a client's machine. */
  private val _epoch = 1
}
