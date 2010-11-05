//
// $Id$

package coreen.util

import java.io.{BufferedInputStream, File, FileOutputStream}
import java.net.URL
import java.util.jar.{JarFile, JarInputStream}

import com.samskivert.io.StreamUtil
  
/**
 * Handles the downloading and unpacking of remote archives.
 */
object RemoteUnpacker
{
  /**
   * Unpacks the supplied remote jar or zip file into the specified local directory.
   */
  def unpackJar (url :URL, into :File) {
    val jin = new JarInputStream(new BufferedInputStream(url.openStream))
    var entry = jin.getNextJarEntry
    while (entry != null) {
      val file = new File(into + File.separator + entry.getName)
      if (entry.isDirectory) {
        file.mkdir
      } else {
        StreamUtil.copy(jin, new FileOutputStream(file)).close
      }
      entry = jin.getNextJarEntry
    }
    jin.close
  }
}
