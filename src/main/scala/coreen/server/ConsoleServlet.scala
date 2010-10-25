//
// $Id$

package coreen.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import coreen.rpc.ConsoleService

/** Provides the console servlet. */
trait ConsoleServlet {
  this :Log with Console =>

  /**
   * The implementation of console services provided by {@link ConsoleService}.
   */
  class ConsoleServlet extends RemoteServiceServlet with ConsoleService
  {
    def fetchConsole (id :String, fromLine :Int) :ConsoleService.ConsoleResult = {
      val result = new ConsoleService.ConsoleResult
      result.isOpen = _console.isOpen(id)
      result.lines = _console.fetch(id, fromLine).toArray
      result
    }
  }
}
