//
// $Id$

package coreen.model

import java.util.Date

import coreen.model.{Project => JProject}
import coreen.persist.{Project => SProject}

/**
 * Converts over-the-wire model objects to and from (persistable) Scala representations.
 */
object Convert
{
  /** Converts a Scala Project to a Java Project. */
  def toJava (sp :SProject) :JProject = new JProject(
    sp.id, sp.name, sp.rootPath, sp.version, new Date(sp.imported), new Date(sp.lastUpdated))
}
