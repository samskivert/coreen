//
// $Id$

package coreen.persist

/**
 * A class that handles mapping fully qualfied names to long identifiers, by way of the {@link
 * DB#_db#names} table.
 */
trait DefMap extends Function1[String, Long]
{
  /** Resolves the ids of all supplied fully qualified names. They may subsequently be looked up
   * in this map without incurring database access. */
  def resolveIds (fqNames :Traversable[String]) :Unit

  /** Assigns ids to all supplied fully qualified names. They may subsequently be looked up in this
   * map without incurring database access. */
  def assignIds (fqNames :Traversable[String]) :Unit

  /** Returns true if this map contains the supplied fully qualified name. The name is not resolved
   * from the database. */
  def contains (fqName :String) :Boolean

  /** Returns the id of the supplied fully qualified name, or None if no mapping exists. */
  def get (fqName :String) :Option[Long]
}
