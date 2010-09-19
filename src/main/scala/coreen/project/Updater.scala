//
// $Id$

package coreen.project

import java.io.{File, StringReader}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.xml.{XML, Elem}

import org.squeryl.PrimitiveTypeMode._

import coreen.nml.SourceModel
import coreen.nml.SourceModel._
import coreen.persist.{DB, Project, CompUnit}
import coreen.server.Main
import coreen.server.Main.log

/**
 * Handles the scanning of a project directory for compilation units and the processing thereof.
 */
object Updater
{
  /**
   * (Re)imports the contents of the specified project. This includes:
   * <ul>
   *  <li>scanning the root path of the supplied project for compilation units</li>
   *  <li>grouping them by language</li>
   *  <li>running the appropriate readers to convert them to name-resolved form</li>
   *  <li>clearing the current project contents from the database</li>
   *  <li>loading the name-resolved metadata into the database</li>
   * </ul>
   * This is very disk and compute intensive and should be done on a background thread.
   *
   * @param log a callback function which will be passed log messages to communicate status.
   */
  def update (p :Project, log :String=>Unit = noop => ()) {
    log("Finding compilation units...")

    // first figure out what sort of source files we see in the project
    val types = collectFileTypes(new File(p.rootPath))

    // fire up readers to handle all types of files we find in the project
    val readers = Map() ++ (types flatMap(t => readerForType(t) map(r => (t -> r))))
    log("Processing compilation units of type " + readers.keySet.mkString(", ") + "...")
    readers.values map(_.invoke(p, log))
  }

  abstract class Reader {
    def invoke (p :Project, log :String=>Unit) {
      Main.log.info("Invoking reader: " + (args :+ p.rootPath).mkString(" "))
      val proc = Runtime.getRuntime.exec((args :+ p.rootPath).toArray)
      var accum = new StringBuilder
      var accmode = false

      val cubuf = ArrayBuffer[CompUnitElem]()

      // consume stdout from the reader, accumulating <compunit ...>...</compunit> into a buffer
      // and processing each complete unit that we receive; anything in between compunit elements
      // is reported verbatim to the status log
      for (line <- Source.fromInputStream(proc.getInputStream).getLines) {
        accmode = accmode || line.trim.startsWith("<compunit");
        if (!accmode) log(line)
        else {
          accum.append(line)
          accmode = !line.trim.startsWith("</compunit>")
          if (!accmode) {
            try {
              val cu = SourceModel.parse(XML.load(new StringReader(accum.toString)))
              if (cu.src.startsWith(p.rootPath)) cu.src = cu.src.substring(p.rootPath.length+1)
              cubuf += cu
            } catch {
              case e => log("Error parsing reader output [" + e + "]: " +
                            truncate(accum.toString, 100))
            }
            accum.setLength(0)
          }
        }
      }

      // copy any output from stderr to the status log now that stdout is closed (TODO: really this
      // should happen on another thread because it's possible for the reader to fill up its stderr
      // buffer and block, and we'll deadlock waiting for stdout to close)
      Source.fromInputStream(proc.getErrorStream).getLines.foreach(log)

      // report any error status code (TODO: we probably don't really need to do this)
      val ecode = proc.waitFor
      if (ecode != 0) {
        log("Reader exited with status: " + ecode)
        return // leave the project as is; TODO: maybe not if this is the first import...
      }

      // determine which CUs we knew about before
      val cus = cubuf.toList
      val oldPaths = transaction {
        from(DB.compunits)(cu => where(cu.projectId === p.id) select(cu.path)) toSet
      }

      val newPaths = Set() ++ (cus map(_.src))
      val toDelete = oldPaths -- newPaths
      val toAdd = newPaths -- oldPaths
      val toUpdate = newPaths -- toAdd
      transaction {
        if (!toDelete.isEmpty) {
          DB.compunits.deleteWhere(cu => cu.path in toDelete)
          log("Removed " + toDelete.size + " obsolete compunits.")
        }
        val now = System.currentTimeMillis
        if (!toAdd.isEmpty) {
          DB.compunits.insert(toAdd.map(CompUnit(p.id, _, now)))
          log("Added " + toAdd.size + " new compunits.")
        }
        if (!toUpdate.isEmpty) {
          DB.compunits.update(cu =>
            where(cu.path in toUpdate) set(cu.lastUpdated := now))
          log("Updated " + toUpdate.size + " compunits.")
        }
      }
    }

    def args :List[String]
  }

  abstract class JavaReader (
    classname :String, classpath :List[String], javaArgs :List[String]
  ) extends Reader {
    def args = ("java" :: "-classpath" :: classpath.mkString(File.pathSeparator) ::
                classname :: javaArgs)
  }

  // TODO: get these paths from a config file
  class JavaJavaReader extends JavaReader(
    "coreen.java.Main",
    List(System.getProperty("java.home") + "/../lib/tools.jar",
         "java-reader/target/scala_2.8.0/coreen-java-reader_2.8.0-0.1.min.jar"),
    List())

  def readerForType (typ :String) :Option[Reader] = typ match {
    case "java" => Some(new JavaJavaReader)
    case _ => None
  }

  def collectFileTypes (file :File) :Set[String] = {
    def suffix (name :String) = name.substring(name.lastIndexOf(".")+1)
    if (file.isDirectory) file.listFiles.toSet flatMap(collectFileTypes)
    else Set(suffix(file.getName))
  }

  def truncate (text :String, length :Int) =
    if (text.length <= length) text
    else text.substring(0, length) + "..."
}
