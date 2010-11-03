//
// $Id$

package coreen.nml

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests the source code model classes (parsing, etc.).
 */
class SourceModelSpec extends FlatSpec with ShouldMatchers
{
  val sample = <compunit src="test/path">
    <def start="5" end="12" name="TestA" type="TYPE" kind="class">
      <def start="34" end="48" name="A" type="TYPE" kind="none">
        <def start="70" end="81" name="value" type="TERM" kind="none">
          <use start="77" end="77" target="int"></use>
        </def>
      </def>
      <def start="106" end="120" name="B" type="TYPE" kind="none">
        <def start="142" end="154" name="noop" type="FUNC" kind="none">
        </def>
      </def>
      <def start="196" end="215" name="main" type="FUNC" kind="none">
        <def start="221" end="230" name="args" type="TERM" kind="none">
          <use start="221" end="227" target="java.lang.String[]"></use>
        </def>
        <def start="250" end="254" name="av" type="TERM" kind="none">
          <use start="250" end="250" target="int"></use>
        </def>
        <def start="286" end="288" name="b" type="TERM" kind="none">
          <use start="286" end="286" target="TestA.B"></use>
        </def>
      </def>
    </def>
  </compunit>

  "Model" should "parse some simple XML" in {
    val u = SourceModel.parse(sample)
    u.src should equal("test/path")
    u.getDef("TestA.A").get.name should equal("A")
    u.getDef("TestA.A.value").get.name should equal("value")
    u.getDef("TestA.A.oops") should equal(None)
    u.getDef("TestA.B").get.name should equal("B")
    u.getDef("TestA.B.noop").get.name should equal("noop")
    u.getDef("TestA.main.args").get.name should equal("args")
  }
}
