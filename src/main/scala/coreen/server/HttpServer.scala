//
// $Id$

package coreen.server

import java.io.File
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import scala.io.Source

import org.mortbay.jetty.Server
import org.mortbay.jetty.handler.ContextHandlerCollection
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.DefaultServlet
import org.mortbay.jetty.servlet.ServletHolder

import coreen.rpc.LibraryService
import coreen.rpc.ProjectService

import Main.log

/**
 * Customizes a Jetty server and handles HTTP requests.
 */
class HttpServer extends Server
{
  def init {
    // use a custom connector that works around some jetty non-awesomeness
    setConnectors(Array(
      new SelectChannelConnector {
        setHost(_config.getBindHostname)
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
      log.warning("Unable to infer document root from location of '" + SENTINEL + "'.")
    } else {
      val stlpath = stlurl.toExternalForm
      log.info(stlpath)
      ctx.setResourceBase(stlpath.substring(0, stlpath.length-SENTINEL.length))
    }
    ctx.setWelcomeFiles(Array[String]("index.html"))
    // wire up our servlets
    ctx.addServlet(new ServletHolder(_shutdownServlet), "/coreen/shutdown")
    ctx.addServlet(new ServletHolder(_libServlet), "/coreen/"+LibraryService.ENTRY_POINT)
    ctx.addServlet(new ServletHolder(_projServlet), "/coreen/"+ProjectService.ENTRY_POINT)
    ctx.addServlet(new ServletHolder(new CoreenDefaultServlet), "/*")
    addHandler(ctx)

    // if there's another Coreen running, tell it to step aside
    try {
      val locurl = "http://" + _config.getBindHostname + ":" + _config.getHttpPort
      val rsp = Source.fromURL(locurl + "/coreen/shutdown").getLines.mkString("\n")
      if (!rsp.equals("byebye")) {
        log.warning("Got weird repsonse when shutting down existing server: " + rsp)
      }
    } catch {
      case ce :java.net.ConnectException => // no other server, no problem!
      case e => log.warning("Not able to shutdown local server: " + e)
    }
  }

  def shutdown {
    try {
      stop
    } catch {
      case e => log.warning("Failed to stop HTTP server.", e)
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

  // TODO: inject these?
  protected val _config :ServerConfig = new ServerConfig {
    def getBindHostname = "localhost"
    def getHttpPort = 8080
  }
  protected val _libServlet :LibraryServlet = new LibraryServlet
  protected val _projServlet :ProjectServlet = new ProjectServlet

  protected val _shutdownServlet = new HttpServlet() {
    override def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
      val out = rsp.getWriter
      out.write("byebye")
      out.close
      Main.shutdown
    }
  }

  protected val ONE_YEAR = 365*24*60*60*1000L
  protected val SENTINEL = "coreen/index.html"
}
