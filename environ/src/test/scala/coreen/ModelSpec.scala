//
// $Id$

package coreen

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests the source code model classes (parsing, etc.).
 */
class ModelSpec extends FlatSpec with ShouldMatchers
{
  val sample = <def start="5" end="12" name="TestA">
                 <def start="34" end="48" name="A">
                   <def start="70" end="81" name="value">
                     <use start="77" end="77" target="int"></use>
                   </def>
                 </def>
                 <def start="106" end="120" name="B">
                   <def start="142" end="154" name="noop">
                   </def>
                 </def>
                 <def start="196" end="215" name="main">
                   <def start="221" end="230" name="args">
                     <use start="221" end="227" target="java.lang.String[]"></use>
                   </def>
                   <def start="250" end="254" name="av">
                     <use start="250" end="250" target="int"></use>
                   </def>
                   <def start="286" end="288" name="b">
                     <use start="286" end="286" target="TestA.B"></use>
                   </def>
                 </def>
               </def>

  "Model" should "parse some simple XML" in {
    val d = Model.parse(sample)
    d.getDef("TestA.A").get.name should equal("A")
    d.getDef("TestA.A.value").get.name should equal("value")
    d.getDef("TestA.A.oops") should equal(None)
    d.getDef("TestA.B").get.name should equal("B")
    d.getDef("TestA.B.noop").get.name should equal("noop")
    d.getDef("TestA.main.args").get.name should equal("args")
  }
}
