//
// $Id$

package coreen.server

/** A base trait for components that handles lifecycle management. */
trait Component {
  /** Called to initialize this component.
   *  If the component overrides this method, it must call `super.initComponents`. */
  protected def initComponents { /* nada */ }

  /** Called to start this component.
   *  If the component overrides this method, it must call `super.startComponents`. */
  protected def startComponents { /* nada */ }

  /** Called to shut this component down.
   *  If the component overrides this method, it must call `super.shutdownComponents`. */
  protected def shutdownComponents { /* nada */ }
}
