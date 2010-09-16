//
// $Id$

package coreen.project

import java.io.File

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests random parts of the Updater.
 */
class UpdaterSpec extends FlatSpec with ShouldMatchers
{
  "findCompUnits" should "find compilation units" in {
    val sentinel = getClass.getClassLoader.getResource("com/test/Test.java")
    sentinel should not equal(null)
    sentinel.getProtocol should equal("file")

    val sidx = sentinel.getPath.indexOf("com/test/Test.java")
    sidx should not equal(-1)
    val root = new File(sentinel.getPath.substring(0, sidx))
    root.isDirectory should equal(true)

    Updater.findCompUnits(root).size should equal(3)
  }
}
