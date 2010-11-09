//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains the id, start, length and kind of a def. For use in decorating signatures.
 */
public class SigDef implements Serializable, Span
{
    /** This def's unique identifier. */
    public long id;

    /** The kind of this def. */
    public Kind kind;

    /** The character offset in the source file at which this def starts. */
    public int start;

    /** The length of this def's text. */
    public int length;

    /** Creates and initializes this instance. */
    public SigDef (long id, Kind kind, int start, int length)
    {
        this.id = id;
        this.kind = kind;
        this.start = start;
        this.length = length;
    }

    /** Used when unserializing. */
    public SigDef () {}

    // from interface Span
    public long getId ()
    {
        return id;
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
        return id + "(" + kind + ":" + start + ":" + length + ")";
    }
}
