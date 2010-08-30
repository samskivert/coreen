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

  "Model" should "parse XML properly" in {
    println(Model.parse(sample))
  }
}
