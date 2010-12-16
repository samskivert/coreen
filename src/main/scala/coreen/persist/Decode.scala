//
// $Id$

package coreen.persist

import coreen.model.{Flavor, Kind}

/** Contains mappings for converting between Java enums and ints for storage in the database. */
object Decode {
  /** Maps {@link Kind} elements to an Int that can be used in the DB. */
  val kindToCode = Map(
    Kind.MODULE -> 1,
    Kind.TYPE -> 2,
    Kind.FUNC -> 3,
    Kind.TERM -> 4,
    Kind.UNKNOWN -> 0
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Kind}. */
  val codeToKind = kindToCode map { case(x, y) => (y, x) }

  /** Maps {@link Flavor} elements to an Int that can be used in the DB. */
  val flavorToCode = Map(
    // module flavors (none)

    // type flavors
    Flavor.CLASS -> 10,
    Flavor.INTERFACE -> 11,
    Flavor.ABSTRACT_CLASS -> 12,
    Flavor.ENUM -> 13,
    Flavor.ANNOTATION -> 14,
    Flavor.OBJECT -> 15,
    Flavor.ABSTRACT_OBJECT -> 16,
    Flavor.TYPE_PARAM -> 17,

    // func flavors
    Flavor.METHOD -> 30,
    Flavor.ABSTRACT_METHOD -> 31,
    Flavor.STATIC_METHOD -> 32,
    Flavor.CONSTRUCTOR -> 33,

    // term flavors
    Flavor.FIELD -> 50,
    Flavor.PARAM -> 51,
    Flavor.LOCAL -> 52,
    Flavor.STATIC_FIELD -> 53,

    // universal flavors
    Flavor.NONE -> 0
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Flavor}. */
  val codeToFlavor = flavorToCode map { case(x, y) => (y, x) }
}
