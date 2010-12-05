//
// $Id$

package coreen.server

import java.util.concurrent.{ExecutorService, Executors, TimeUnit}

/** Provides a means by which to execute background tasks. */
trait Exec {
  trait Executator {
    type Job = (() => Unit)

    /** Returns an executor service for low-level scheduling. */
    def svc :ExecutorService

    /** Returns lists of (active, queued) jobs. */
    def jobs :(Seq[String], Seq[String])

    /** Queues a job with the specified description to be executed on the executor immediately. */
    def executeJob (descrip :String, job :Job)

    /** Queues a job with the specified description to be executed on the executor after all
     * previous queued jobs are completed. */
    def queueJob (descrip :String, job :Job)
  }

  /** An executor for invoking background tasks. */
  val _exec :Executator
}

/** A concrete implementation of {@link Exec}. */
trait ExecComponent extends Component with Exec with Log {
  /** Mixer can override this to customize thread pool size. */
  protected def threadPoolSize = 4

  val _exec = new Executator {
    def svc = _esvc

    def jobs = synchronized {
      (_active, _jobs.map(_._1))
    }

    def executeJob (descrip :String, job :Job) = submitJob(descrip, job, false)

    def queueJob (descrip :String, job :Job) = synchronized {
      _log.info("Queueing: " + descrip)
      _jobs :+= (descrip -> job)
      maybeExecuteNextJob()
    }

    protected def maybeExecuteNextJob () :Unit = synchronized {
      if (_active.isEmpty && !_jobs.isEmpty) {
        val next = _jobs.head
        _jobs = _jobs.tail
        submitJob(next._1, next._2, true)
      }
    }

    protected def submitJob (descrip :String, job :Job, trigger :Boolean) = synchronized {
        _active :+= descrip
        _log.info("Executing: " + descrip)
        _esvc.execute(new Runnable() {
          def run () = {
            job()
            jobComplete(descrip, trigger)
          }
        })
    }

    protected def jobComplete (descrip :String, trigger :Boolean) = synchronized {
      _active = _active.filterNot(_ == descrip)
      if (trigger) maybeExecuteNextJob()
    }

    protected var _jobs = Seq[(String, Job)]()
    protected var _active = Seq[String]()
  }

  val _esvc = Executors.newFixedThreadPool(threadPoolSize)

  override protected def shutdownComponents {
    super.shutdownComponents
    _esvc.shutdown
    _esvc.awaitTermination(60, TimeUnit.SECONDS)
  }
}
