//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Identifies the use of a definition that exists in a source file.
 */
public class Use
    implements Serializable
{
    /** A unique identifier for this use (1 or higher). */
    public long id;

    /** The id of the immediately enclosing definition in which this use occurs. */
    public long ownerId;

    /** The id of the definition of the referent of this use. */
    public long referentId;

    /** The location in the source file of this use. */
    public Span loc;

    /** Creates and initializes this instance. */
    public Use (long id, long ownerId, long referentId, Span loc)
    {
        this.id = id;
        this.ownerId = ownerId;
        this.referentId = referentId;
        this.loc = loc;
    }

    /** Used when unserializing. */
    public Use () {}

    @Override // from Object
    public String toString ()
    {
        return new StringBuffer("[id=").append(id).
            append(", owner=").append(ownerId).
            append(", referent=").append(referentId).
            append(", loc=").append(loc).
            append("]").toString();
    }
}
