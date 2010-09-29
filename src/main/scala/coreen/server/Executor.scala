//
// $Id$

package coreen.server

import java.util.concurrent.ExecutorService

/** Provides a means by which to execute background tasks. */
trait ExecutorModule
{
  /** An executor for invoking background tasks. */
  val _exec :ExecutorService
}
