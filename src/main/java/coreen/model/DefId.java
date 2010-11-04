//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains the id, name and kind of a def.
 */
public class DefId implements Serializable
{
    /** This def's unique identifier. */
    public long id;

    /** This def's name. */
    public String name;

    /** The kind of this def. */
    public Kind kind;

    @Override // from Object
    public String toString ()
    {
        return name + "(" + kind + ":" + id + ")";
    }
}
