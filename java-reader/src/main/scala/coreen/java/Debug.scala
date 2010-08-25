//
// $Id$

package coreen.java

/**
 * Some routines for debugging.
 */
object Debug
{
  val DEBUG = java.lang.Boolean.getBoolean("coreen.debug")

  /**
   * Emits a debug message to stderr.
   *
   * @param args key/value pairs, (e.g. "age", someAge, "size", someSize) which will be appended
   * to the log message as [age=someAge, size=someSize].
   */
  def log (message :String, args :Any*) {
    if (DEBUG) System.err.println(format(message, args:_*));
  }

  /**
   * Emits a temporary debugging message to stderr.
   *
   * @param args key/value pairs, (e.g. "age", someAge, "size", someSize) which will be appended
   * to the log message as [age=someAge, size=someSize].
   */
  def temp (message :String, args :Any*) {
    System.err.println("*** " + format(message, args:_*));
  }

  /**
   * Emits a warning message to stderr.
   *
   * @param args key/value pairs, (e.g. "age", someAge, "size", someSize) which will be appended
   * to the log message as [age=someAge, size=someSize].
   */
  def warn (message :String, args :Any*) {
    System.err.println("!!! " + format(message, args:_*));
  }

  /**
   * Formats a debug message.
   *
   * @param args key/value pairs, (e.g. "age", someAge, "size", someSize) which will be appended
   * to the log message as [age=someAge, size=someSize].
   */
  def format (message :String, args :Any*) = {
    message + (if (args.isEmpty) "" else " [" + (args grouped(2) map(p => p.length match {
      case 1 => p(0) + "=<odd arg>"
      case 2 => p(0) + "=" + toString(p(1))
    }) mkString(", ")) + "]")
  }

  /**
   * Generates a (hopefully) informative string from the supplied argument.
   *
   * @param arg the argument to be turned into a string, may be null.
   */
  def toString (arg :Any) = try { String.valueOf(arg) }
                            catch { case t => "<toString() failure: " + t + ">" }
}
