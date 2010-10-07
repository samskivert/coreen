//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Contains a def type and id. Used for both defs and uses in different circumstances.
 */
public class TypedId implements Serializable
{
    /** The type of def. */
    public Def.Type type;

    /** The type of the referent. */
    public long id;

    /** Creates and initializes this instance. */
    public TypedId (Def.Type type, long id)
    {
        this.type = type;
        this.id = id;
    }

    /** Used when unserializing. */
    public TypedId () {}

    @Override // from Object
    public String toString ()
    {
        return type + ":" + id;
    }
}
