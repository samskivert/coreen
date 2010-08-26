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
    public final int id;

    /** This definition's enclosing definition, or null if none. */
    public final Def parent;

    /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
    public final String name;

    /** The type of this definition (function, term, etc.). */
    public final Type type;

    /** Definitions nested within this one. */
    public final Def[] defs;

    /** The character offset in the file at which this definition's body starts. */
    public final int bodyStart;

    /** Uses that occur immediately within this definition (i.e. not in nested definitions). */
    public final Use[] uses;

    public Def (int id, Def parent, String name, Type type, Def[] defs, int bodyStart, Use[] uses,
                int start, int length) {
        super(start, length);
        this.id = id;
        this.parent = parent;
        this.name = name;
        this.type = type;
        this.defs = defs;
        this.bodyStart = bodyStart;
        this.uses = uses;
    }
}
