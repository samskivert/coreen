//
// $Id$

package coreen.server

/**
 * The main entry point for the Coreen server.
 */
object Main
{
  val log = com.samskivert.util.Logger.getLogger("coreen")

  def main (args :Array[String]) {
    val httpServer = new HttpServer
    httpServer.init
  }
}
