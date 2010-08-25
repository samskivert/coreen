//
// $Id$

package coreen.java

import com.sun.tools.javac.Main

/**
 * Provides an API for convering Java source to name-resolved source (NRS).
 */
object Reader {
  def process (files :Seq[String]) = {
    Main.compile(files.toArray);
  }
}
