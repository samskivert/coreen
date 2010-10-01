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
    /** The id of the definition of the referent of this use. */
    public long referentId;

    /** The location in the source file of this use. */
    public Span loc;

    /** Creates and initializes this instance. */
    public Use (long referentId, Span loc)
    {
        this.referentId = referentId;
        this.loc = loc;
    }

    /** Used when unserializing. */
    public Use () {}

    @Override // from Object
    public String toString ()
    {
        return new StringBuffer("[referent=").append(referentId).
            append(", loc=").append(loc).
            append("]").toString();
    }
}
