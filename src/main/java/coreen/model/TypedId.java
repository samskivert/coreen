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

    /** The name of the referent. */
    public String name;

    /** Creates and initializes this instance. */
    public TypedId (Def.Type type, long id, String name)
    {
        this.type = type;
        this.id = id;
        this.name = name;
    }

    /** Used when unserializing. */
    public TypedId () {}

    @Override // from Object
    public String toString ()
    {
        return name + "(" + type + ":" + id + ")";
    }
}
