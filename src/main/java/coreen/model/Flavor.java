//
// $Id$

package coreen.model;

/**
 * Defines different flavors of definition.
 */
public enum Flavor
{
    // type flavors

    /** A plain old class (Java, etc.). */
    CLASS,
    /** An interface (Java, etc.). */
    INTERFACE,
    /** An abstract class (Java, etc.). */
    ABSTRACT_CLASS,
    /** An enumeration (Java, etc.). */
    ENUM,
    /** An annotation type (Java, etc.). */
    ANNOTATION,
    /** A singleton object (Scala). */
    OBJECT,
    /** An abstract singleton object (Scala). */
    ABSTRACT_OBJECT,
    /** A type parameter. */
    TYPE_PARAM,

    // func flavors

    /** A normal class method (Java, etc.). */
    METHOD,
    /** An abstract (or interface) method (Java, etc.). */
    ABSTRACT_METHOD,
    /** A static method (Java, etc.). */
    STATIC_METHOD,
    /** An object constructor (Java, etc.). */
    CONSTRUCTOR,

    // term flavors

    /** An object field (Java, etc.). */
    FIELD,
    /** A class field (Java, etc.). */
    STATIC_FIELD,
    /** A function parameter (Java, etc.). */
    PARAM,
    /** A local variable (Java, etc.). */
    LOCAL,

    /** A flavorless def. */
    NONE;
}
