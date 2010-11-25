//
// $Id$

package coreen.server

import java.io.File
import java.net.{URI, URL}
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import scala.io.Source

import org.mortbay.jetty.Server
import org.mortbay.jetty.handler.ContextHandlerCollection
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.DefaultServlet
import org.mortbay.jetty.servlet.ServletHolder

import coreen.rpc.{ConsoleService, LibraryService, ProjectService}

/** Provides HTTP services. */
trait Http {
  this :Log with Dirs with Console with Config
            with LibraryServlet with ProjectServlet with ConsoleServlet with ServiceServlet =>

  /** Returns a URI that can be used to communicate with the Coreen server. */
  def getServerURL (path :String) =
    new URL("http", _config.getHttpHostname, _config.getHttpPort, "/coreen/" + path)

  /** Customizes a Jetty server and handles HTTP requests. */
  class HttpServer extends Server {
    def init {
      // use a custom connector that works around some jetty non-awesomeness
      setConnectors(Array(
        new SelectChannelConnector {
          setHost(_config.getHttpHostname)
          setPort(_config.getHttpPort)
        }
      ))

      // this will magically cause addHandler to work and dispatch to our contexts
      setHandler(new ContextHandlerCollection)

      // wire up our management servlet
      val ctx = new Context
      ctx.setContextPath("/")
      // locate our sentinel resource and use that to compute our document root
      val stlurl = classOf[HttpServer].getClassLoader.getResource(SENTINEL)
      if (stlurl == null) {
        _log.warning("Unable to infer document root from location of '" + SENTINEL + "'.")
      } else {
        val stlpath = stlurl.toExternalForm
        ctx.setResourceBase(stlpath.substring(0, stlpath.length-SENTINEL.length))
      }
      ctx.setWelcomeFiles(Array[String]("index.html"))
      // wire up our servlets
      ctx.addServlet(new ServletHolder(new LibraryServlet), "/coreen/"+LibraryService.ENTRY_POINT)
      ctx.addServlet(new ServletHolder(new ProjectServlet), "/coreen/"+ProjectService.ENTRY_POINT)
      ctx.addServlet(new ServletHolder(new ConsoleServlet), "/coreen/"+ConsoleService.ENTRY_POINT)
      ctx.addServlet(new ServletHolder(new ServiceServlet), "/coreen/service")
      ctx.addServlet(new ServletHolder(_shutdownServlet), "/coreen/shutdown")
      ctx.addServlet(new ServletHolder(new CoreenDefaultServlet), "/*")
      addHandler(ctx)

      // if there's another Coreen running, tell it to step aside
      try {
        val rsp = Source.fromURL(getServerURL("shutdown")).getLines.mkString("\n")
        if (!rsp.equals("byebye")) {
          _log.warning("Got weird repsonse when shutting down existing server: " + rsp)
        }
      } catch {
        case ce :java.net.ConnectException => // no other server, no problem!
          case e => _log.warning("Not able to shutdown local server: " + e)
      }
    }

    def shutdown {
      try {
        stop
      } catch {
        case e => _log.warning("Failed to stop HTTP server.", e)
      }
    }

    class CoreenDefaultServlet extends DefaultServlet {
      override def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
        val path = req.getPathInfo
        if (path != null) {
          // add a no caching header to the GWT .nocach.js file
          if (path.endsWith(".nocache.js")) {
            rsp.addHeader("Cache-Control", "no-cache, no-store")
          }
          // and cache the unchanging files for long time
          else if (path.indexOf(".cache.") != -1) {
            rsp.setDateHeader("Expires", System.currentTimeMillis + ONE_YEAR)
          }
        }
        super.doGet(req, rsp)
      }
    }

    protected val _shutdownServlet = new HttpServlet() {
      override def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
        try {
          val out = rsp.getWriter
          out.write("byebye")
          out.close
        } finally {
          Coreen.shutdown
        }
      }
    }

    protected val ONE_YEAR = 365*24*60*60*1000L
    protected val SENTINEL = "coreen/index.html"
  }
}

/** A concrete implementation of {@link Http}. */
trait HttpComponent extends Component with Http {
  this :Log with Dirs with Console with Config
            with LibraryServlet with ProjectServlet with ConsoleServlet with ServiceServlet =>

  /** Handles HTTP service. */
  val httpServer = new HttpServer

  override protected def initComponents {
    super.initComponents
    httpServer.init
  }

  override protected def startComponents {
    super.startComponents
    // TODO: if we fail to bind to our port, pop up an AWT dialog allowing the port to be changed
    // (and writing that back to the configuration probably)
    httpServer.start
  }

  override protected def shutdownComponents {
    super.shutdownComponents
    httpServer.shutdown
  }
}
