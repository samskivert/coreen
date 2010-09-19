//
// $Id$

package coreen.server

import java.io.File
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
  val log = com.samskivert.util.Logger.getLogger("coreen")
  val exec = Executors.newFixedThreadPool(4) // TODO: configurable

  // TODO: move into injected ServerConfig, or something
  val coreenDir = new File(System.getProperty("user.home") + File.separator + ".coreen")
  def projectDir (project :String) = new File(new File(coreenDir, "projects"), project)

  def main (args :Array[String]) {
    // create the Coreen data directory if necessary
    if (!coreenDir.isDirectory) {
      if (!coreenDir.mkdir) {
        System.err.println("Failed to create: " + coreenDir.getAbsolutePath)
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
    if (false) transaction { DB.reinitSchema }

    // initialize our Jetty http server
    val httpServer = new HttpServer
    httpServer.init
    httpServer.start

    // register a signal handler to shutdown gracefully on ctrl-c
    val sigint = new Signal("INT")
    var ohandler :SignalHandler = null
    ohandler = Signal.handle(sigint, new SignalHandler {
      def handle (sig :Signal) {
        log.info("Coreen server exiting...")
        Signal.handle(sigint, ohandler) // restore old signal handler
        httpServer.shutdown // shutdown the http server
        exec.shutdown // shutdown the executors
        sigint.synchronized { sigint.notify } // notify the main thread that it's OK to exit
      }
    })
    log.info("Coreen server running. Ctrl-c to exit.")

    // block the main thread until our signal is received
    sigint.synchronized { sigint.wait }
  }
}
