//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Identifies the use of a definition that exists in a source file.
 */
public class Use implements Serializable, Span
{
    /** The id of the definition of the referent of this use. */
    public long referentId;

    /** The kind of the referent of this use. */
    public Kind kind;

    /** The character offset in the source file at which this span starts. */
    public int start;

    /** The length of this use in characters. */
    public int length;

    /** Creates and initializes this instance. */
    public Use (long referentId, Kind kind, int start, int length)
    {
        this.referentId = referentId;
        this.kind = kind;
        this.start = start;
        this.length = length;
    }

    /** Used when unserializing. */
    public Use () {}

    // from interface Span
    public long getId ()
    {
        return referentId;
    }

    // from interface XXX
    public Kind getKind ()
    {
        return kind;
    }

    // from interface Span
    public int getStart ()
    {
        return start;
    }

    // from interface Span
    public int getLength ()
    {
        return length;
    }

    @Override // from Object
    public String toString ()
    {
        return "[ref=" + referentId + ", kind=" + kind + ", loc=" + start + "-" + length + "]";
    }
}
