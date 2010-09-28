//
// $Id$

package coreen.server

import java.io.File
import java.sql.DriverManager
import java.util.concurrent.Executors

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter

import org.squeryl.PrimitiveTypeMode._

import coreen.persist.DB
  
/**
 * Traits that provide access to useful services.
 */
object Services
{
  trait Service {
    protected def initServices { /* nada */ }
    protected def startServices { /* nada */ }
    protected def shutdownServices { /* nada */ }
  }

  trait Log extends Service {
    /** For great logging. */
    val log = com.samskivert.util.Logger.getLogger("coreen")
  }

  trait Dirs extends Service {
    this :Log =>

    /** Our local data directory. */ // TODO: move into injected ServerConfig, or something
    val coreenDir = new File(System.getProperty("user.home") + File.separator + ".coreen")

    /** Returns the local data directory for a project with the supplied identifier. */
    def projectDir (project :String) = new File(new File(coreenDir, "projects"), project)

    /** Whether or not this is the first time the tool/app has been run on this machine.
     * We use the non-existence of the .coreen directory as an indicator of first-run-hood. */
    val firstTime = !coreenDir.isDirectory

    override protected def initServices {
      super.initServices

      // create the Coreen data directory if necessary
      if (firstTime) {
        if (!coreenDir.mkdir) {
          log.warning("Failed to create: " + coreenDir.getAbsolutePath)
          System.exit(255)
        }
      }
    }
  }

  trait Database extends Service {
    this :Dirs with Log =>

    /** Mixer can override this to log database queries. */
    protected def dblogger :(String => Unit) = null

    override protected def initServices {
      super.initServices

      // initialize the H2 database
      Class.forName("org.h2.Driver")
      val dburl = "jdbc:h2:" + new File(coreenDir, "repository").getAbsolutePath
      SessionFactory.concreteFactory = Some(() => {
        // TODO: use connection pools as Squeryl creates and closes a connection on every query
        val sess = Session.create(DriverManager.getConnection(dburl, "sa", ""), new H2Adapter)
        sess.setLogger(dblogger)
        sess
      })

      // TODO: squeryl doesn't support any sort of schema migration; sigh
      if (firstTime) transaction { DB.reinitSchema }
    }
  }

  trait Executor extends Service {
    this :Log =>

    /** An executor for invoking background tasks. */
    val exec = Executors.newFixedThreadPool(4) // TODO: configurable

    override protected def shutdownServices {
      super.shutdownServices
      exec.shutdown
    }
  }

  trait HTTP extends Service {
    this :Log =>

    /** Handles HTTP service. */
    val httpServer = new HttpServer

    override protected def initServices {
      super.initServices
      httpServer.init
    }

    override protected def startServices {
      super.startServices
      httpServer.start
    }

    override protected def shutdownServices {
      super.shutdownServices
      httpServer.shutdown
    }
  }
}
