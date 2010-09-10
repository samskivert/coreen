//
// $Id$

package coreen.server

import java.io.File

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.mortbay.jetty.Server
import org.mortbay.jetty.handler.ContextHandlerCollection
import org.mortbay.jetty.nio.SelectChannelConnector
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.DefaultServlet
import org.mortbay.jetty.servlet.ServletHolder

import coreen.rpc.NaviService
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
    // locate our sentinal resource and use that to compute our document root
    val stlurl = classOf[HttpServer].getClassLoader.getResource(SENTINAL)
    if (stlurl == null) {
      log.warning("Unable to infer document root from location of '" + SENTINAL + "'.")
    } else {
      val stlpath = stlurl.toExternalForm
      ctx.setResourceBase(stlpath.substring(0, stlpath.length-SENTINAL.length))
    }
    ctx.setWelcomeFiles(Array[String]("index.html"))
    // wire up our servlets
    ctx.addServlet(new ServletHolder(_naviServlet), "/coreen/"+NaviService.ENTRY_POINT)
    ctx.addServlet(new ServletHolder(new CoreenDefaultServlet), "/*")
    addHandler(ctx)
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
  protected var _config :ServerConfig = new ServerConfig {
    def getBindHostname = "localhost"
    def getHttpPort = 8080
  }
  protected var _naviServlet :NaviServlet = new NaviServlet

  protected val ONE_YEAR = 365*24*60*60*1000L
  protected val SENTINAL = "coreen/index.html"
}
