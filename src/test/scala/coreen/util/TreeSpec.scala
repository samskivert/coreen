//
// $Id$

package coreen.util

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests our Tree.
 */
class TreeSpec extends FlatSpec with ShouldMatchers
{
  "Tree" should "do some basic stuff" in {
    val root = new Tree[Int]()
    root.add(List("one", "two", "three"), 5)

    root.get(List("four")) should equal(None)
    root("one").value should equal(None)
    root.get(List("one")) should equal (None)
    root("one")("two").value should equal(None)
    root.get(List("one", "two")) should equal (None)
    root("one")("two")("three").value should equal(Some(5))
    root.get(List("one", "two", "three")) should equal (Some(5))

    root.add(List("one", "two"), 3)

    root.get(List("four")) should equal(None)
    root("one").value should equal(None)
    root.get(List("one")) should equal (None)
    root("one")("two").value.get should equal(3)
    root.get(List("one", "two")) should equal (Some(3))
    root("one")("two")("three").value.get should equal(5)
    root.get(List("one", "two", "three")) should equal (Some(5))
    root.get(List("one", "two", "three", "four")) should equal (None)
  }
}
