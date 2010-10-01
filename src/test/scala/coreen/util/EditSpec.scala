//
// $Id$

package coreen.util

import scalaj.collection.Imports._

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests our little applier of edits.
 */
class EditSpec extends FlatSpec with ShouldMatchers
{
  //          01234567890123456789012345
  val text = "Now is the time for all..."

  "Edit.applyEdits" should "handle the basic case" in {
    val edits = List(new Edit(4, "<b>"), new Edit(6, "</b>"))
    Edit.applyEdits(edits.asJava, text) should equal("Now <b>is</b> the time for all...")
  }

  "Edit.applyEdits" should "handle edits at the start of the text" in {
    val edits = List(new Edit(0, "<b>"), new Edit(3, "</b>"))
    Edit.applyEdits(edits.asJava, text) should equal("<b>Now</b> is the time for all...")
  }

  "Edit.applyEdits" should "handle edits at the end of the text" in {
    val edits = List(new Edit(20, "<b>"), new Edit(26, "</b>"))
    Edit.applyEdits(edits.asJava, text) should equal("Now is the time for <b>all...</b>")
  }

  "Edit.applyEdits" should "throw StringIndexOutOfBoundsException on negative edits" in {
    val edits = List(new Edit(-1, "<b>"))
    evaluating {
      Edit.applyEdits(edits.asJava, text)
    } should produce[StringIndexOutOfBoundsException]
  }

  "Edit.applyEdits" should "throw StringIndexOutOfBoundsException on OOB edits" in {
    val edits = List(new Edit(29, "<b>"))
    evaluating {
      Edit.applyEdits(edits.asJava, text)
    } should produce[StringIndexOutOfBoundsException]
  }
}
