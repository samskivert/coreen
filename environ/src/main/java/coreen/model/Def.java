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
    public static enum Type {
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
    public final long id;

    /** The id of this definition's enclosing definition, or null if none. */
    public final long parentId;

    /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
    public final String name;

    /** The type of this definition (function, term, etc.). */
    public final Type type;

    /** The character offset in the file at which this definition's body starts. */
    public final int bodyStart;

    /** The location in the source file of this definition. */
    public final Span loc;

    public Def (long id, long parentId, String name, Type type, int bodyStart, Span loc) {
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.bodyStart = bodyStart;
        this.loc = loc;
    }

    @Override // from Object
    public String toString () {
        return new StringBuffer("[id=").append(id).
            append(", parent=").append(parentId).
            append(", name=").append(name).
            append(", type=").append(type).
            append(", bstart=").append(bodyStart).
            append(", loc=").append(loc).
            append("]").toString();
    }
}
