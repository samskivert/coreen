//
// $Id$

package coreen.server

/**
 * Provides configuration for the Coreen server.
 */
trait ServerConfig
{
  def getBindHostname :String
  def getHttpPort :Int
}
