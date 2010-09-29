//
// $Id$

package coreen.server

/** A base trait for services that handles lifecycle management. */
trait Service {
  /** Called to initialize this service.
   *  If the service overrides this method, it must call `super.initServices`. */
  protected def initServices { /* nada */ }

  /** Called to start this service.
   *  If the service overrides this method, it must call `super.startServices`. */
  protected def startServices { /* nada */ }

  /** Called to shut this service down.
   *  If the service overrides this method, it must call `super.shutdownServices`. */
  protected def shutdownServices { /* nada */ }
}
