//
// $Id$

package coreen.persist

/**
 * A class that handles mapping fully qualfied names to long identifiers, by way of the {@link
 * DB#_db#names} table.
 */
trait DefMap extends Function1[String, Long]
{
  /** Resolves the ids of all supplied fully qualified names. They may subsequently be looked up in
   * this map without incurring database access.
   * @param andChildren if true, all children of the specified ids will also be loaded. */
  def resolveIds (fqNames :Traversable[String], andChildren :Boolean) :Unit

  /** Assigns ids to all supplied fully qualified names. They may subsequently be looked up in this
   * map without incurring database access. */
  def assignIds (fqNames :Traversable[String]) :Unit

  /** Returns true if this map contains the supplied fully qualified name. */
  def contains (fqName :String) :Boolean = get(fqName).isDefined

  /** Returns the id of the supplied fully qualified name, or throw NoSuchElementException. */
  def apply (fqName :String) :Long = get(fqName).get

  /** Returns the id of the supplied fully qualified name, or None if no mapping exists. */
  def get (fqName :String) :Option[Long]
}
