//
// $Id$

package coreen.model;

/**
 * Identifies a definition that exists in a source file.
 */
public class Def extends Span
{
    /** Defines various types of definitions. */
    public static enum Type {
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
    public final int id;

    /** The id of this definition's enclosing definition, or null if none. */
    public final int parentId;

    /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
    public final String name;

    /** The type of this definition (function, term, etc.). */
    public final Type type;

    /** The character offset in the file at which this definition's body starts. */
    public final int bodyStart;

    public Def (int id, int parentId, String name, Type type, int bodyStart,
                int start, int length) {
        super(start, length);
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.bodyStart = bodyStart;
    }

    @Override // from Object
    protected StringBuffer toString (StringBuffer buf) {
        return super.toString(buf.append("id=").append(id).
                              append(", parent=").append(parentId).
                              append(", name=").append(name).
                              append(", type=").append(type).
                              append(", bstart=").append(bodyStart).append(", "));
    }
}
