//
// $Id$

package coreen.project

import java.io.File
import scala.io.Source

import coreen.persist.Project
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
   * @param sfunc a callback function which will be passed status strings.
   */
  def update (p :Project, sfunc :String=>Unit = noop => ()) {
    sfunc("Finding compilation units...")

    val units = findCompUnits(new File(p.rootPath))
    val umap = units groupBy(_.lang)

    sfunc("Processing " + units.size + " compilation units...")

    // create a directory in which to hold our temporary bits
    val uproot = new File(Main.projectDir(p.name), "update")
    uproot.mkdirs()

    // TODO: this is all a giant hack!
    execJava("coreen.java.Main",
             List(System.getProperty("java.home") + "/../lib/tools.jar",
                  "java-reader/target/scala_2.8.0/coreen-java-reader_2.8.0-0.1.min.jar"),
             "--out" :: uproot.getAbsolutePath :: (umap("java") map(_.file.getAbsolutePath)))
  }

  private[project] def execJava (classname :String, classpath :List[String], args :List[String]) {
    val cmd = "java" :: "-classpath" :: classpath.mkString(File.pathSeparator) :: classname :: args
    log.info("Running " + cmd)
    val proc = Runtime.getRuntime.exec(cmd.toArray)
    log.info("stderr " + Source.fromInputStream(proc.getErrorStream).getLines.mkString("\n"))
    log.info("stdout " + Source.fromInputStream(proc.getInputStream).getLines.mkString("\n"))
    log.info("Updater " + proc.waitFor)
  }

  private[project] def findCompUnits (file :File) :List[CompUnit] = {
    def suffix (name :String) = name.substring(name.lastIndexOf(".")+1)
    if (file.isDirectory) file.listFiles.toList flatMap(findCompUnits)
    else suffix(file.getName) match {
      case "java" => List(CompUnit(file, "java"))
      case _ => List()
    }
  }

  private[project] case class CompUnit (file :File, lang :String)
}
