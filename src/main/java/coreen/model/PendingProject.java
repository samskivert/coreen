//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains information on a project being imported into the repository.
 */
public class PendingProject
    implements Serializable
{
    /** The source provided by the user from which to import the project data.
     * @see LibraryService#importProject */
    public String source;

    /** A human readable string indicating the current import status. */
    public String status;

    /** The time at which this import was started. */
    public long started;

    /** The time at which this import last made progress. */
    public long lastUpdated;

    /** Non-zero once this project is fully imported. */
    public long projectId;

    /** Creates and initializes this instance. */
    public PendingProject (String source, String status, long started, long lastUpdated,
                           long projectId)
    {
        this.source = source;
        this.status = status;
        this.started = started;
        this.lastUpdated = lastUpdated;
        this.projectId = projectId;
    }

    /** Used when unserializing. */
    public PendingProject () {}

    /**
     * Returns true if this project import has completed.
     */
    public boolean isComplete ()
    {
        return projectId != 0L;
    }

    /**
     * Returns true of this project import is complete and has failed.
     */
    public boolean isFailed ()
    {
        return projectId < 0L;
    }
}
