//
// $Id$

package coreen.model

import java.util.Date

import coreen.model.{Project => JProject, CompUnit => JCompUnit}
import coreen.persist.{Project => SProject, CompUnit => SCompUnit}

/**
 * Converts over-the-wire model objects to and from (persistable) Scala representations.
 */
object Convert
{
  /** Converts a Scala Project to a Java Project. */
  def toJava (sp :SProject) :JProject = new JProject(
    sp.id, sp.name, sp.rootPath, sp.version, new Date(sp.imported), new Date(sp.lastUpdated))

  /** Converts a Scala CompUnit to a Java CompUnit. */
  def toJava (cu :SCompUnit) :JCompUnit = new JCompUnit(cu.id, cu.projectId, cu.path)
}
