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
    sp.id, sp.name, sp.rootPath, sp.version, sp.srcDirs.getOrElse(""),
    new Date(sp.imported), new Date(sp.lastUpdated))

  /** Converts a Scala CompUnit to a Java CompUnit. */
  def toJava (cu :SCompUnit) :JCompUnit = new JCompUnit(cu.id, cu.projectId, cu.path)

  /** Converts a Scala Def to a Java Def. */
  def toJava (decode :Map[Int,JDef.Type])(d :SDef) :JDef = initDef(decode, d, new JDef)

  /** Converts a Scala Use to a Java Use. */
  def toJava (u :SUse) :JUse = new JUse(u.referentId, u.useStart, u.useEnd-u.useStart)

  /** Converts a Scala Def to a TypedId. */
  def toTypedId (decode :Map[Int,JDef.Type])(d :SDef) :TypedId =
    new TypedId(decode(d.typ), d.id, d.name)

  /** Converts a Scala Def to a TypeSummary.Member. */
  def toMember (decode :Map[Int,JDef.Type])(d :SDef) :TypeSummary.Member =
    initMember(decode, d, new TypeSummary.Member)

  private def initDef (decode :Map[Int,JDef.Type], sdef :SDef, jdef :JDef) = {
    jdef.id = sdef.id
    jdef.parentId = sdef.parentId
    jdef.name = sdef.name
    jdef.`type` = decode(sdef.typ)
    jdef.start = sdef.defStart
    jdef
  }

  private def initMember (decode :Map[Int,JDef.Type], sdef :SDef, mem :TypeSummary.Member) = {
    initDef(decode, sdef, mem)
    mem.doc = sdef.doc.getOrElse(null)
    mem.sig = sdef.sig.getOrElse(null)
    mem
  }
}
