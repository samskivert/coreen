//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Models a single source file.
 */
public class CompUnit
    implements Serializable
{
    /** A unique identifier for this compilation unit (1 or higher). */
    public final long id;

    /** The id of the project to which this compilation unit belongs. */
    public final long projectId;

    /** The path (relative to the project root) to this compilation unit. */
    public final String path;

    public CompUnit (long id, long projectId, String path)
    {
        this.id = id;
        this.projectId = projectId;
        this.path = path;
    }

    @Override // from Object
    public String toString ()
    {
        return new StringBuffer("[id=").append(id).
            append(", project=").append(projectId).
            append(", path=").append(path).
            append("]").toString();
    }
}
