//
// $Id$

package coreen.model

import java.io.{DataOutputStream, ByteArrayOutputStream, DataInputStream, ByteArrayInputStream}
import java.util.Date

import coreen.model.{Project => JProject, CompUnit => JCompUnit, Def => JDef, Use => JUse}
import coreen.persist.{Decode, Sig}
import coreen.persist.{Project => SProject, CompUnit => SCompUnit, Def => SDef, Use => SUse}

/**
 * Converts over-the-wire model objects to and from (persistable) Scala representations.
 */
object Convert
{
  /** Converts a Scala Project to a Java Project. */
  def toJava (sp :SProject) :JProject = new JProject(
    sp.id, sp.name, sp.rootPath, sp.version, sp.srcDirs.getOrElse(""), sp.readerOpts.getOrElse(""),
    new Date(sp.imported), new Date(sp.lastUpdated))

  /** Converts a Scala CompUnit to a Java CompUnit. */
  def toJava (cu :SCompUnit) :JCompUnit = new JCompUnit(cu.id, cu.projectId, cu.path)

  /** Converts a Scala Def to a Java Def. */
  def toJava (d :SDef) :JDef = initDef(d, new JDef)

  /** Converts a Scala Def to a DefId. */
  def toDefId (d :SDef) :DefId = initDefId(d, new DefId)

  /** Converts a Scala Def to a DefInfo. */
  def toDefInfo (d :SDef, s :Option[Sig]) :DefInfo = initDefInfo(d, s, new DefInfo)

  /** Converts a Scala Use to a Java Use. */
  def toJava (u :SUse) :JUse =
    new JUse(u.referentId, Decode.codeToKind(u.kind), u.useStart, u.useEnd-u.useStart)

  /** Initializes a DefId from a Scala Def. */
  def initDefId[DT <: DefId] (sdef :SDef, jdef :DT) = {
    jdef.id = sdef.id
    jdef.name = sdef.name
    jdef.kind = Decode.codeToKind(sdef.kind)
    jdef.start = sdef.defStart
    jdef
  }

  /** Initializes a Java Def from a Scala Def. */
  def initDef[DT <: JDef] (sdef :SDef, jdef :DT) = {
    initDefId(sdef, jdef)
    jdef.flavor = Decode.codeToFlavor(sdef.flavor)
    jdef.flags = sdef.flags;
    jdef.outerId = sdef.outerId
    jdef.superId = sdef.superId
    jdef
  }

  /** Initializes a DefInfo from a Scala Def. */
  def initDefInfo[DT <: DefInfo] (sdef :SDef, sig :Option[Sig], mem :DT) = {
    initDef(sdef, mem)
    mem.sig = sig map(_.text) getOrElse("<missing signature>")
    mem.sigUses = sig map(s => decodeUses(s.uses)) getOrElse(Array[JUse]())
    mem.doc = sdef.doc getOrElse(null)
    mem
  }

  /** Encodes a collection of uses into binary blob form. */
  def encodeUses (uses :Seq[JUse]) :Array[Byte] = {
    // val data = Array.ofDim[Byte](uses.size * (8+4+4+4))
    val bout = new ByteArrayOutputStream
    val out = new DataOutputStream(bout)
    for (use <- uses) {
      out.writeLong(use.referentId)
      out.writeInt(Decode.kindToCode(use.kind))
      out.writeInt(use.start)
      out.writeInt(use.length)
    }
    bout.toByteArray
    // data
  }

  /** Decodes a collection of uses from binary blob form. */
  def decodeUses (data :Array[Byte]) :Array[JUse] = {
    val in = new DataInputStream(new ByteArrayInputStream(data))
    val count = data.length/USE_BYTES
    val out = Array.ofDim[JUse](count)
    for (ii <- 0 until count) {
      out(ii) = new JUse(in.readLong, Decode.codeToKind(in.readInt), in.readInt, in.readInt)
    }
    out
  }

  private[this] val USE_BYTES = 8+4+4+4
}
