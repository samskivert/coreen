//
// $Id$

package coreen.model

import java.util.Date

import coreen.model.{Project => JProject, CompUnit => JCompUnit, Def => JDef, Use => JUse}
import coreen.persist.{Project => SProject, CompUnit => SCompUnit, Def => SDef, Use => SUse}

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

  /** Converts a Scala Def to a Java Def. */
  def toJava (decode :Map[Int,JDef.Type])(d :SDef) :JDef = new JDef(
    d.id, d.parentId, d.name, decode(d.typ), d.defStart)

  /** Converts a Scala Use to a Java Use. */
  def toJava (u :SUse) :JUse = new JUse(u.referentId, u.useStart, u.useEnd-u.useStart)
}
