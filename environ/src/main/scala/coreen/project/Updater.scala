//
// $Id$

package coreen.project

import java.io.File

import coreen.persist.Project

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
   */
  def update (p :Project) {
    val units = findCompUnits(new File(p.rootPath))
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
