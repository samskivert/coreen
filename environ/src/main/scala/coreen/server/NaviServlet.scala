//
// $Id$

package coreen.server

import coreen.model.{Def, Project}
import coreen.rpc.NaviService

/**
 * The implementation of navigation services provided by {@link NaviService}.
 */
class NaviServlet extends NaviService
{
  // from interface NaviService
  def getProjects :Array[Project] = {
    List(new Project(1, "/home/mdb/projects/samskivert", "samskivert")).toArray
  }

  // from interface NaviService
  def  getToTypeDefs (projectId :Long) :Array[Def] = {
    null // TODO
  }

  // from interface NaviService
  def getToMethodDefs (projectId :Long) :Array[Def] = {
    null // TODO
  }
}
