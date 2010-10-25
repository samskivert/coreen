//
// $Id$

package coreen.server

import java.io.{File, PrintStream, FileOutputStream, IOException}

import sun.misc.{Signal, SignalHandler}

import coreen.persist.{DB, DBComponent}
import coreen.project.{Importer, Updater}

/**
 * The main entry point for the Coreen server.
 */
object Coreen extends AnyRef
  with LogComponent with DirsComponent with ExecComponent with DBComponent with ConsoleComponent
  with HttpComponent with ProjectServlet with LibraryServlet with ConsoleServlet
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

    startComponents // start our components

    // if we're running in app mode, open a web browser
    if (_appdir.isDefined) {
      LaunchCmds find(cmd => try {
        Runtime.getRuntime.exec((cmd :+ LaunchURL).toArray).waitFor != 0
      } catch {
        case ex => false // move on to the next command
      })
    }

    _log.info("Coreen running. Ctrl-c to exit.")

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

  private val LaunchCmds = List(
    List("xdg-open"), // gnome
    List("open"), // mac os x
    List("cmd.exe", "/c", "start")) // vinders
  private val LaunchURL = "http://localhost:8080/coreen/"
}
