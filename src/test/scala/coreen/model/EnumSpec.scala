//
// $Id$

package coreen.model

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests decoding our enums.
 */
class EnumSpec extends FlatSpec with ShouldMatchers
{
  "Kind" should "be decodable from string" in {
    decodeKind("class") should equal(Kind.CLASS)
    decodeKind("interface") should equal(Kind.INTERFACE)
    decodeKind("Abstract_Class") should equal(Kind.ABSTRACT_CLASS)
    decodeKind("eNuM") should equal(Kind.ENUM)
    decodeKind("OBJECT") should equal(Kind.OBJECT)
  }

  def decodeKind (text :String) = Enum.valueOf(classOf[Kind], text.toUpperCase)
}
