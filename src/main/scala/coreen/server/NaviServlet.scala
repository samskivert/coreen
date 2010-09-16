//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import coreen.model.Def
import coreen.rpc.NaviService

/**
 * The implementation of navigation services provided by {@link NaviService}.
 */
class NaviServlet extends RemoteServiceServlet with NaviService
{
  // from interface NaviService
  def  getToTypeDefs (projectId :Long) :Array[Def] = {
    null // TODO
  }

  // from interface NaviService
  def getToMethodDefs (projectId :Long) :Array[Def] = {
    null // TODO
  }
}
