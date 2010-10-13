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
  "Flavor" should "be decodable from string" in {
    decodeFlavor("class") should equal(Flavor.CLASS)
    decodeFlavor("interface") should equal(Flavor.INTERFACE)
    decodeFlavor("Abstract_Class") should equal(Flavor.ABSTRACT_CLASS)
    decodeFlavor("eNuM") should equal(Flavor.ENUM)
    decodeFlavor("OBJECT") should equal(Flavor.OBJECT)
  }

  def decodeFlavor (text :String) = Enum.valueOf(classOf[Flavor], text.toUpperCase)
}
