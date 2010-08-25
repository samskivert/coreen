//
// $Id$

package coreen.java

import java.io.ByteArrayOutputStream
import java.io.PrintStream

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

class DebugSpec extends FlatSpec with ShouldMatchers
{
  "Debug.toString" should "handle null" in {
    Debug.toString(null) should equal("null")
  }

  "Debug.toString" should "handle failure" in {
    class Bomb {
      override def toString = throw new Exception("Boom!")
    }
    Debug.toString(new Bomb) should equal("<toString() failure: java.lang.Exception: Boom!>")
  }

  "Debug.format" should "handle no args" in {
    Debug.format("test") should equal("test")
  }

  "Debug.format" should "handle args" in {
    Debug.format("test", "bob", null, "jim", 3) should equal("test [bob=null, jim=3]")
  }

  "Debug.temp" should "not mangle the args" in {
    val olderr = System.err
    val errbuf = new ByteArrayOutputStream
    System.setErr(new PrintStream(errbuf))
    Debug.temp("test", "bob", null, "jim", 3)
    System.setErr(olderr)
    new String(errbuf.toByteArray) should equal(
      "*** " + Debug.format("test", "bob", null, "jim", 3) + "\n")
  }
}
