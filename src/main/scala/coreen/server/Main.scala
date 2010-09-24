//
// $Id$

package coreen.server

import java.io.{File, PrintStream, FileOutputStream, IOException}
import java.sql.DriverManager
import java.util.concurrent.Executors

import sun.misc.{Signal, SignalHandler}

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._

import coreen.project.Importer
import coreen.persist.DB

/**
 * The main entry point for the Coreen server.
 */
object Main
{
  /** For great logging. */
  val log = com.samskivert.util.Logger.getLogger("coreen")

  /** An executor for invoking background tasks. */
  val exec = Executors.newFixedThreadPool(4) // TODO: configurable

  /** Our application install directory iff we're running in app mode. */
  val appdir = Option(System.getProperty("appdir")) map(new File(_))

  /** Our local data directory. */ // TODO: move into injected ServerConfig, or something
  val coreenDir = new File(System.getProperty("user.home") + File.separator + ".coreen")

  /** Returns the local data directory for a project with the supplied identifier. */
  def projectDir (project :String) = new File(new File(coreenDir, "projects"), project)

  def main (args :Array[String]) {
    // if we're running via Getdown, redirect our log output to a file
    appdir map { appdir =>
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
          log.warning("Failed to open debug log", "path", nlog, "error", ioe)
      }
    }

    // create the Coreen data directory if necessary
    val firstTime = !coreenDir.isDirectory
    if (firstTime) {
      if (!coreenDir.mkdir) {
        log.warning("Failed to create: " + coreenDir.getAbsolutePath)
        System.exit(255)
      }
    }

    // initialize the H2 database
    Class.forName("org.h2.Driver")
    val dburl = "jdbc:h2:" + new File(coreenDir, "repository").getAbsolutePath
    SessionFactory.concreteFactory = Some(() => {
      // TODO: use connection pools as Squeryl creates and closes a connection on every query
      val sess = Session.create(DriverManager.getConnection(dburl, "sa", ""), new H2Adapter)
      sess.setLogger(s=>println(s))
      sess
    })

    // TODO: squeryl doesn't support any sort of schema migration; sigh
    if (firstTime) transaction { DB.reinitSchema }

    // initialize our Jetty http server
    val httpServer = new HttpServer
    httpServer.init
    httpServer.start

    // register a signal handler to shutdown gracefully on ctrl-c
    var ohandler :SignalHandler = null
    ohandler = Signal.handle(_sigint, new SignalHandler {
      def handle (sig :Signal) {
        shutdown
      }
    })
    log.info("Coreen server running. Ctrl-c to exit.")

    // if we're running in app mode, open a web browser
    if (appdir.isDefined) {
      LaunchCmds find(cmd => try {
        Runtime.getRuntime.exec((cmd :+ LaunchURL).toArray).waitFor != 0
      } catch {
        case ex => false // move on to the next command
      })
    }

    // block the main thread until our signal is received
    _sigint.synchronized { _sigint.wait }

    log.info("Coreen server exiting...")
    Signal.handle(_sigint, ohandler) // restore old signal handler
    httpServer.shutdown // shutdown the http server
    exec.shutdown // shutdown the executors
  }

  def shutdown {
    _sigint.synchronized { _sigint.notify } // notify the main thread that it's OK to exit
  }

  private val _sigint = new Signal("INT")

  private val LaunchCmds = List(
    List("xdg-open"), // gnome
    List("open"), // mac os x
    List("cmd.exe", "/c", "start")) // vinders
  private val LaunchURL = "http://localhost:8080/coreen/"
}
