//
// $Id$

package coreen.server

import java.io.File

/** Provides directory-related metadata. */
trait Dirs {
  /** Our application install directory iff we're running in app mode. */
  val _appdir :Option[File]

  /** Our local data directory. */
  val _coreenDir :File

  /** Whether or not this is the first time the tool/app has been run on this machine.
   *  We use the non-existence of the .coreen directory as an indicator of first-run-hood. */
  lazy val _firstTime = !_coreenDir.isDirectory

  /** Returns the local data directory for a project with the supplied identifier. */
  def _projectDir (project :String) = new File(new File(_coreenDir, "projects"), project)
}

/** A concrete implementation of {@link Dirs}. */
trait DirsComponent extends Component with Dirs {
  this :Log =>

  val _appdir = Option(System.getProperty("appdir")) map(new File(_))
  val _coreenDir = {
    val dname = if (_appdir.isDefined) ".coreen" else ".coreendev"
    new File(System.getProperty("user.home") + File.separator + dname)
  }

  override protected def initComponents {
    super.initComponents

    // create the Coreen data directory if necessary
    if (_firstTime) {
      if (!_coreenDir.mkdir) {
        _log.warning("Failed to create: " + _coreenDir.getAbsolutePath)
        System.exit(255)
      }
    }
  }
}
