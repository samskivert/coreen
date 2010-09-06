//
// $Id$

package coreen.server

import sun.misc.{Signal, SignalHandler}

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
    val sigint = new Signal("INT")
    var ohandler :SignalHandler = null
    ohandler = Signal.handle(sigint, new SignalHandler {
      def handle (sig :Signal) {
        log.info("Coreen server exiting...")
        Signal.handle(sigint, ohandler) // restore old signal handler
        httpServer.shutdown
        sigint.synchronized { sigint.notify }
      }
    })
    log.info("Coreen server running. Ctrl-c to exit.")

    // block the main thread until our signal is received
    sigint.synchronized { sigint.wait }
  }
}
