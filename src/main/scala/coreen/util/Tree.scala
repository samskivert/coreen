//
// $Id$

package coreen.util

import scala.collection.mutable.{Map => MMap}

/**
 * A tree where every node optionally contains a value and where children are identified by name.
 */
class Tree[T] (var value :Option[T], val children :MMap[String, Tree[T]])
  extends Function1[String, Tree[T]]
{
  /** Creates a tree node with the no value and no children. */
  def this () = this(None, MMap[String, Tree[T]]())

  /** Creates a tree node with the supplied value and no children. */
  def this (value :T) = this(Some(value), MMap[String, Tree[T]]())

  /** Adds a child to this node with the specified name and value. */
  def add (name :String, value :T) {
    get(name).value = Some(value)
  }

  /** Adds a value to the tree at the supplied path. Intermediate nodes are created as needed with
   * None values.
   * @throws IllegalArgumentException thrown if a value is already contained in the tree at the
   * specified path. */
  def add (path :Seq[String], value :T) {
    path.length match {
      case 0 => throw new IllegalArgumentException("Empty path (value " + value + ")")
      case 1 => add(path.head, value)
      case _ => get(path.head).add(path.tail, value)
    }
  }

  /** Returns the child with the specified key, or excepts. */
  def apply (key :String) = children.apply(key)

  /** Returns the child with the specified key, adding a new child if necessary. */
  def get (key :String) :Tree[T] = children.getOrElseUpdate(key, new Tree[T]())

  /** Returns the value for the specified path, or None. */
  def get (key :Seq[String]) :Option[T] =
    if (key.length == 0) value
    else {
      val c = children.get(key.head)
      if (c.isDefined) c.get.get(key.tail)
      else None
    }
}
