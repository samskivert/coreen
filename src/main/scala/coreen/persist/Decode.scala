//
// $Id$

package coreen.persist

import coreen.model.{Kind, Type}

/** Contains mappings for converting between Java enums and ints for storage in the database. */
object Decode {
  /** Maps {@link Type} elements to an Int that can be used in the DB. */
  val typeToCode = Map(
    Type.MODULE -> 1,
    Type.TYPE -> 2,
    Type.FUNC -> 3,
    Type.TERM -> 4,
    Type.UNKNOWN -> 0
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Type}. */
  val codeToType = typeToCode map { case(x, y) => (y, x) }

  /** Maps {@link Kind} elements to an Int that can be used in the DB. */
  val kindToCode = Map(
    // module kinds (none)

    // type kinds
    Kind.CLASS -> 10,
    Kind.INTERFACE -> 11,
    Kind.ABSTRACT_CLASS -> 12,
    Kind.ENUM -> 13,
    Kind.ANNOTATION -> 14,
    Kind.OBJECT -> 15,
    Kind.ABSTRACT_OBJECT -> 16,

    // func kinds
    Kind.METHOD -> 30,
    Kind.ABSTRACT_METHOD -> 31,
    Kind.STATIC_METHOD -> 32,
    Kind.CONSTRUCTOR -> 33,

    // term kinds
    Kind.FIELD -> 50,
    Kind.PARAM -> 51,
    Kind.LOCAL -> 52,
    Kind.STATIC_FIELD -> 53,

    // universal kinds
    Kind.NONE -> 0
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Kind}. */
  val codeToKind = kindToCode map { case(x, y) => (y, x) }
}
