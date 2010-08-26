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
        TERM
    };

    /** A unique identifier for this definition (1 or higher). */
    public int id;

    /** The id of this definitions enclosing definition, or 0 if none. */
    public int parentId;

    /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
    public String name;

    /** The type of this definition (function, term, etc.). */
    public Type type;

    /** The character offset in the file at which this definition's body starts. */
    public int bodyStart;

    public Def (int id, int parentId, String name, Type type, int bodyStart, int start, int length) {
        super(start, length);
        this.id = id;
        this.parentId = parentId;
        this.name = name;
        this.type = type;
        this.bodyStart = bodyStart;
    }
}
