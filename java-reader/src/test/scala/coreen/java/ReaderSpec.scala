//
// $Id$

package coreen.java

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests the Java to name-resolved source translator.
 */
class ReaderSpec extends FlatSpec with ShouldMatchers
{
  "Reader" should "handle this code" in {
    println(Reader.process("TestA.java", """
    public class TestA {
        public static class A {
            public int value;
        }
        public static class B {
            public void noop () {
            }
        }
        public static void main (String[] args) {
            int av = new A().value;
            B b = new B();
            b.noop();
        }
    }"""))
  }
}
