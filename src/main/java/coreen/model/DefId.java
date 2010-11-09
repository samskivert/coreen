//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains the id, name and kind of a def.
 */
public class DefId implements Serializable, Span
{
    /** This def's unique identifier. */
    public long id;

    /** This def's name. */
    public String name;

    /** The kind of this def. */
    public Kind kind;

    /** The character offset in the source file at which this def starts. */
    public int start;

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
        return name.length();
    }

    @Override // from Object
    public String toString ()
    {
        return name + "(" + kind + ":" + id + ")";
    }
}
