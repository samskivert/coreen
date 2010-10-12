//
// $Id$

package coreen.model;

/**
 * Defines the different types of definitions.
 */
public enum Type
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
}
