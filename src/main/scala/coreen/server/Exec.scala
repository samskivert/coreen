//
// $Id$

package coreen.server

import java.util.concurrent.{ExecutorService, Executors}

/** Provides a means by which to execute background tasks. */
trait Exec {
  /** An executor for invoking background tasks. */
  val _exec :ExecutorService
}

/** A concrete implementation of {@link Exec}. */
trait ExecComponent extends Component {
  /** Mixer can override this to customize thread pool size. */
  protected def threadPoolSize = 4

  val _exec = Executors.newFixedThreadPool(threadPoolSize)

  override protected def shutdownComponents {
    super.shutdownComponents
    _exec.shutdown
  }
}
