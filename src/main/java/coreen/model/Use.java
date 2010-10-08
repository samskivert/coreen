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

    /** The character offset in the source file at which this span starts. */
    public int start;

    /** The length of this use in characters. */
    public int length;

    /** Creates and initializes this instance. */
    public Use (long referentId, int start, int length)
    {
        this.referentId = referentId;
        this.start = start;
        this.length = length;
    }

    /** Used when unserializing. */
    public Use () {}

    @Override // from Object
    public String toString ()
    {
        return "[ref=" + referentId + ", loc=" + start + "-" + length + "]";
    }
}
