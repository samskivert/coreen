//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains the id, start, length and kind of a def. For use in decorating signatures.
 */
public class SigDef implements Serializable
{
    /** This def's unique identifier. */
    public long id;

    /** The kind of this def. */
    public Kind kind;

    /** The character offset in the source file at which this def starts. */
    public int start;

    /** The length of this def's text. */
    public int length;

    @Override // from Object
    public String toString ()
    {
        return id + "(" + kind + ":" + start + ":" + length + ")";
    }
}
