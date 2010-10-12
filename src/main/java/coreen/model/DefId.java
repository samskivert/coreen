//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains the id, name and type of a def.
 */
public class DefId implements Serializable
{
    /** The type of the referent. */
    public long id;

    /** The name of the referent. */
    public String name;

    /** The type of this def. */
    public Type type;

    @Override // from Object
    public String toString ()
    {
        return name + "(" + type + ":" + id + ")";
    }
}
