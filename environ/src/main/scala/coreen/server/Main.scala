//
// $Id$

package coreen.server

import com.samskivert.util.SignalUtil

/**
 * The main entry point for the Coreen server.
 */
object Main
{
  val log = com.samskivert.util.Logger.getLogger("coreen")

  def main (args :Array[String]) {
    // initialize our various components
    val httpServer = new HttpServer
    httpServer.init
    httpServer.start

    // register a signal handler to shutdown gracefully on ctrl-c
    val pause = new AnyRef
    SignalUtil.register(SignalUtil.Number.INT, new SignalUtil.Handler {
      def signalReceived (num :SignalUtil.Number) {
        log.info("Coreen server exiting...")
        httpServer.shutdown
        pause.synchronized { pause.notify }
      }
    })
    log.info("Coreen server running. Ctrl-c to exit.")

    // block the main thread until our signal is received
    pause.synchronized { pause.wait }
  }
}
