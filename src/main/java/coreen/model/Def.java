//
// $Id$

package coreen.model;

import java.io.Serializable;

/**
 * Identifies a definition that exists in a source file.
 */
public class Def
    implements Serializable
{
    /** Defines various types of definitions. */
    public static enum Type
    {
        /** A package or other module definition. */
        MODULE,
        /** A class, record, object or other type definition. */
        TYPE, // TODO: should we include closures and blocks as types? if so rename?
        /** A procedure, method or other function definition. */
        FUNC,
        /** A parameter, field, local variable or other term definition. */
        TERM,
        /** Used when parsing fails or something is otherwise amiss. */
        UNKNOWN
    };

    /** A unique identifier for this definition (1 or higher). */
    public long id;

    /** A unique identifier for this definition's enclosing def (or 0 if they have none). */
    public long parentId;

    /** This definition's (unqualified) name (i.e. Foo, not com.bar.Outer.Foo). */
    public String name;

    /** The type of this definition (function, term, etc.). */
    public Type type;

    /** The character offset in the source file at which this def starts. */
    public int start;

    /** Creates and initializes this instance. */
    public Def (long id, long parentId, String name, Type type, int start)
    {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.start = start;
    }

    /** Used when unserializing. */
    public Def () {}

    @Override // from Object
    public String toString ()
    {
        return new StringBuffer("[id=").append(id).
            append(", name=").append(name).
            append(", type=").append(type).
            append("]").toString();
    }
}
