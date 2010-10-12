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
  def toJava (decode :Map[Int,Type])(d :SDef) :JDef = initDef(decode, d, new JDef)

  /** Converts a Scala Def to a DefId. */
  def toDefId (decode :Map[Int,Type])(d :SDef) :DefId = initDefId(decode, d, new DefId)

  /** Converts a Scala Def to a DefInfo. */
  def toDefInfo (decode :Map[Int,Type])(d :SDef) :DefInfo = initDefInfo(decode, d, new DefInfo)

  /** Converts a Scala Use to a Java Use. */
  def toJava (u :SUse) :JUse = new JUse(u.referentId, u.useStart, u.useEnd-u.useStart)

  /** Initializes a DefId from a Scala Def. */
  def initDefId[DT <: DefId] (decode :Map[Int,Type], sdef :SDef, jdef :DT) = {
    jdef.id = sdef.id
    jdef.name = sdef.name
    jdef.`type` = decode(sdef.typ)
    jdef
  }

  /** Initializes a Java Def from a Scala Def. */
  def initDef[DT <: JDef] (decode :Map[Int,Type], sdef :SDef, jdef :DT) = {
    initDefId(decode, sdef, jdef)
    jdef.parentId = sdef.parentId
    jdef.start = sdef.defStart
    jdef
  }

  /** Initializes a DefInfo from a Scala Def. */
  def initDefInfo[DT <: DefInfo] (decode :Map[Int,Type], sdef :SDef, mem :DT) = {
    initDef(decode, sdef, mem)
    mem.sig = sdef.sig.getOrElse(null)
    mem.doc = sdef.doc.getOrElse(null)
    mem
  }
}
