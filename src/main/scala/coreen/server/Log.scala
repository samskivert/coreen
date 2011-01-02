//
// $Id$

package coreen.server

/** Provides logging services. */
trait Log {
  /** An interface for logging. */
  trait Logger {
    /** Logs a message at the debug level.
     * @param args a series of key/value pairs and an optional final exception. */
    def debug (msg :Any, args :Any*)

    /** Logs a message at the info level.
     * @param args a series of key/value pairs and an optional final exception. */
    def info (msg :Any, args :Any*)

    /** Logs a message at the warning level.
     * @param args a series of key/value pairs and an optional final exception. */
    def warning (msg :Any, args :Any*)

    /** Logs a message at the error level.
     * @param args a series of key/value pairs and an optional final exception. */
    def error (msg :Any, args :Any*)

    /** Formats a debug message.
     * @param args key/value pairs, (e.g. "age", someAge, "size", someSize) which will be appended
     * to the log message as [age=someAge, size=someSize]. */
    def format (message :Any, args :Any*) :String

    /** Activates log messages at or above the specified level.
     * @param level 0 for debug+, 1 for info+, 2 for warning+, 3 for error. */
    def setLogLevel (level :Int)
  }

  /** For great logging. */
  val _log :Logger
}

/** A concrete implementation of {@link Log}. */
trait LogComponent extends Component with Log {
  /** For great logging. */
  val _log = new Logger {
    def debug (msg :Any, args :Any*) =
      if (_level <= 0) doLog(0, format(msg, args :_*), getError(args :_*))
    def info (msg :Any, args :Any*) =
      if (_level <= 1) doLog(1, format(msg, args :_*), getError(args :_*))
    def warning (msg :Any, args :Any*) =
      if (_level <= 2) doLog(2, format(msg, args :_*), getError(args :_*))
    def error (msg :Any, args :Any*) =
      if (_level <= 3) doLog(3, format(msg, args :_*), getError(args :_*))

    def setLogLevel (level :Int) {
      require(0 <= _level && _level <= 3, "Log level must be: 0 <= level <= 3")
      _level = level
    }

    def format (message :Any, args :Any*) = {
      val sb = new StringBuilder().append(message)
      if (!args.isEmpty) sb.append(" [").append(formatArgs(args)).append("]")
      sb.toString
    }

    protected def formatArgs (args :Seq[Any]) = args.grouped(2).map(_ match {
      case Seq(k, v) => Some(k + "=" + safeToString(v))
      case Seq(a) => a match {
        case ex :Throwable => None
        case arg => Some(arg + "=<odd arg>")
      }
    }).toList.flatten.mkString(", ")

    protected def safeToString (arg :Any) =
      try { String.valueOf(arg) }
      catch { case t => "<toString() failure: " + t + ">" }

    protected def getError (args :Any*) =
      if (args.length % 2 == 0) None else args.last match {
        case exn :Throwable => Some(exn)
        case _ => None
      }

    protected def doLog (level :Int, fmsg :String, error :Option[Throwable]) {
      val sb = new StringBuffer
      _date.setTime(System.currentTimeMillis)
      _format.format(_date, sb, _fpos)
      sb.append(" ").append(LevelNames(level)).append(" ").append(fmsg)
      val out = if (level > 1) System.err else System.out
      out.println(sb)
      error foreach { _.printStackTrace(out) }
    }

    private var _level = 1
    private val _date = new java.util.Date
    private val _format = new java.text.SimpleDateFormat("yyyy/MM/dd HH:mm:ss:SSS")
    private val _fpos = new java.text.FieldPosition(java.text.DateFormat.DATE_FIELD)

    private val LevelNames = Array("DEBUG", "INFO", "WARN", "ERROR")
  }
}
