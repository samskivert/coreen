//
// $Id$

package coreen.query

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests our CQL parser.
 */
class ParserSpec extends FlatSpec with ShouldMatchers
{
  "Parser" should "handle individual operators" in {
    // logic
    println(Parser.parse("isBlank && toString"))
    println(Parser.parse("isBlank || toString"))

    // type
    println(Parser.parse("foo :Bar"))

    // // data
    // println(Parser.parse(">>foo"))
    // println(Parser.parse("<<foo"))
    // println(Parser.parse(">>>foo"))
    // println(Parser.parse(">>>foo"))

    // // heir
    // println(Parser.parse("-Comparable"))
    // println(Parser.parse("+Comparable"))
    // println(Parser.parse("--Comparable"))
    // println(Parser.parse("++Comparable"))

    // // use
    // println(Parser.parse("@StringUtil"))
    // println(Parser.parse("apply()"))
    // println(Parser.parse("?=monkey"))
    // println(Parser.parse("monkey=?"))

    // // kind
    // println(Parser.parse("M util"))
    // println(Parser.parse("T StringUtil"))
    // println(Parser.parse("F toString"))
    // println(Parser.parse("V monkey"))
  }

  "Parser" should "combinations of operators" in {
    println(Parser.parse("(isBlank||toString)"))
    println(Parser.parse("StringUtil.(isBlank||toString)"))
  }
}
