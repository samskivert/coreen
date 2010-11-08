//
// $Id$

package coreen.model

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import SourceModel._

/**
 * Tests the source code model classes (parsing, etc.).
 */
class SourceModelSpec extends FlatSpec with ShouldMatchers
{
  val example =
    <compunit src="file:/TestA.java">
      <def access="public" start="13" kind="module" flavor="none" name="foo.bar" id="foo.bar">
        <sig>foo.bar</sig>
        <def access="public" bodyEnd="359" start="-1" kind="type" flavor="class" bodyStart="26"
             name="TestA" supers="java.lang.Object" id="foo.bar.TestA">
          <sig>public class TestA
            <sigdef start="13" kind="type" name="TestA"></sigdef>
          </sig>
          <doc></doc>
          <def access="public" bodyEnd="118" start="43" kind="type" flavor="class" bodyStart="55"
               name="A" supers="java.lang.Object" id="foo.bar.TestA.A">
            <sig>public static class A
              <sigdef start="20" kind="type" name="A"></sigdef>
            </sig>
            <doc></doc>
            <def access="public" bodyEnd="108" start="102" kind="term" flavor="field" bodyStart="91"
                 name="value" id="foo.bar.TestA.A.value">
              <sig>public int value
                <sigdef start="11" kind="term" name="value"></sigdef>
              </sig>
              <doc></doc>
            </def>
          </def>
          <def access="public" bodyEnd="208" start="-1" kind="type" flavor="class" bodyStart="127"
               name="B" supers="java.lang.Object" id="foo.bar.TestA.B">
            <sig>public static class B
              <sigdef start="20" kind="type" name="B"></sigdef>
            </sig>
            <doc></doc>
            <def access="public" bodyEnd="198" start="175" kind="func" flavor="method"
                 bodyStart="163" name="noop" supers="" id="foo.bar.TestA.B.noop()void">
              <sig>public void noop()
                <sigdef start="12" kind="func" name="noop"></sigdef>
              </sig>
              <doc></doc>
            </def>
          </def>
          <def access="public" bodyEnd="353" start="236" kind="func" flavor="static_method"
               bodyStart="217" name="main" supers=""
               id="foo.bar.TestA.main(java.lang.String[])void">
            <sig>public static void main(String[] args)
              <use start="24" kind="type" target="java.lang.String" name="String"></use>
              <sigdef start="33" kind="term" name="args"></sigdef>
              <sigdef start="19" kind="func" name="main"></sigdef>
            </sig>
            <doc></doc>
            <def access="default" bodyEnd="255" start="251" kind="term" flavor="param"
                 bodyStart="242" name="args" id="foo.bar.TestA.main(java.lang.String[])void.args">
              <sig>String[] args
                <use start="0" kind="type" target="java.lang.String" name="String"></use>
                <sigdef start="9" kind="term" name="args"></sigdef>
              </sig>
              <doc></doc>
              <use start="242" kind="type" target="java.lang.String" name="String"></use>
            </def>
            <def access="default" bodyEnd="294" start="275" kind="term" flavor="local"
                 bodyStart="271" name="av" id="foo.bar.TestA.main(java.lang.String[])void.av">
              <sig>int av
                <sigdef start="4" kind="term" name="av"></sigdef>
              </sig>
              <doc></doc>
              <use start="284" kind="type" target="foo.bar.TestA.A" name="A"></use>
              <use start="288" kind="term" target="foo.bar.TestA.A.value" name="value"></use>
            </def>
            <def access="default" bodyEnd="321" start="309" kind="term" flavor="local"
                 bodyStart="307" name="b" id="foo.bar.TestA.main(java.lang.String[])void.b">
              <sig>B b
                <use start="0" kind="type" target="foo.bar.TestA.B" name="B"></use>
                <sigdef start="2" kind="term" name="b"></sigdef>
              </sig>
              <doc></doc>
              <use start="307" kind="type" target="foo.bar.TestA.B" name="B"></use>
              <use start="317" kind="type" target="foo.bar.TestA.B" name="B"></use>
            </def>
            <use start="334" kind="term" target="foo.bar.TestA.main(java.lang.String[])void.b"
                 name="b"></use>
            <use start="336" kind="func" target="foo.bar.TestA.B.noop()void" name="noop"></use>
          </def>
        </def>
      </def>
    </compunit>

  // package foo.bar;
  // public class TestA {
  //     public static class A {
  //         public int value;
  //     }
  //     public static class B {
  //         public void noop () {
  //         }
  //     }
  //     public static void main (String[] args) {
  //         int av = new A().value;
  //         B b = new B();
  //         b.noop();
  //     }
  // }

  "Model" should "parse a basic example" in {
    val u = SourceModel.parse(example)
    u.src should equal("file:/TestA.java")
    u.getDef("foo.bar.TestA.A").get.name should equal("A")
    u.getDef("foo.bar.TestA.A.value").get.name should equal("value")
    u.getDef("foo.bar.TestA.A.oops") should equal(None)
    u.getDef("foo.bar.TestA.B").get.name should equal("B")
    u.getDef("foo.bar.TestA.B.noop").get.name should equal("noop")
    u.getDef("foo.bar.TestA.main.args").get.name should equal("args")
    u.getDef("foo.bar.TestA.main").get.sig should equal(
      Some(SigElem("public static void main(String[] args)",
                   List(SigDefElem("args", Kind.TERM, 33),
                        SigDefElem("main", Kind.FUNC, 19)),
                   List(UseElem("String", "java.lang.String", Kind.TYPE, 24)))))
  }
}
