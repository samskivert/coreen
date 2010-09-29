//
// $Id$

package coreen.server

import java.io.File

/** Provides directory-related metadata. */
trait DirsModule
{
  /** Our application install directory iff we're running in app mode. */
  val _appdir :Option[File]
}
