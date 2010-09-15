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

    /** True if this project is fully imported (but is lingering in the list because it was
     * imported recently). */
    public boolean complete;

    /** Creates and initializes this instance. */
    public PendingProject (String source, String status, long started, long lastUpdated,
                           boolean complete)
    {
        this.source = source;
        this.status = status;
        this.started = started;
        this.lastUpdated = lastUpdated;
        this.complete = complete;
    }

    /** Used when unserializing. */
    public PendingProject () {}
}
