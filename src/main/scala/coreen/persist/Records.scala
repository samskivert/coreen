//
// $Id$

package coreen.persist

import org.squeryl.KeyedEntity
import org.squeryl.PrimitiveTypeMode._
import org.squeryl.annotations.Column
import org.squeryl.dsl.CompositeKey2

import coreen.model.DefInfo

/** Used to maintain Coreen configuration. */
case class Setting (
  /** A configuration key. */
  @Column(length=512) key :String,
  /** A configuration value. */
  @Column(length=1024) value :String
) {
  def this () = this("", "")
}

/** Contains project metadata. */
case class Project (
  /** The (human readable) name of this project. */
  name :String,
  /** The path to the root of this project. */
  rootPath :String,
  /** A string identifying the imported version of this project. */
  version :String,
  /** The source directory filters for this project (if any). */
  srcDirs :Option[String],
  /** Options to supply to the reader on the command line (if any). */
  readerOpts :Option[String],
  /** When this project was imported into the library. */
  imported :Long,
  /** When this project was last updated. */
  lastUpdated :Long
) extends KeyedEntity[Long] {
  /* ctor */ { assert(!rootPath.endsWith("/")) }

  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this("", "", "", Some(""), Some(""), 0L, 0L)

  override def toString = "[id=" + id + ", name=" + name + ", vers=" + version + "]"
}

/** Contains metadata for a single compilation unit. */
case class CompUnit (
  /** The id of the project to which this compilation unit belongs. */
  projectId :Long,
  /** The path (relative to the project root) to this compilation unit. */
  @Column(length=1024) path :String,
  /** The time at which this compilation unit was last updated. */
  lastUpdated :Long
) extends KeyedEntity[Long] {
  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", 0L)

  override def toString = "[id=" + id + ", pid=" + projectId + ", path=" + path + "]"
}

/** Contains metadata for a definition. */
case class Def (
  /** A unique identifier for this definition (1 or higher). */
  id :Long,
  /** The id of this definition's enclosing definition, or 0 if none. */
  outerId :Long,
  /** The id of this definition's primary super(type) definition, or 0 if none. */
  superId :Long,
  /** The id of this definition's enclosing compunit. */
  unitId :Long,
  /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
  name :String,
  /** The kind of this definition (function, term, etc.). See {@link Kind}. */
  kind :Int,
  /** The flavor of this definition (class, interface, enum, etc.). See {@link Flavor}. */
  flavor :Int,
  /** Bits for flags. */
  flags :Int,
  /** The character offset in the source file of the start of this definition. */
  defStart :Int,
  /** The character offset in the source file of the end of this definition. */
  defEnd :Int,
  /** The character offset in the file at which this definition's body starts. */
  bodyStart :Int,
  /** The character offset in the file at which this definition's body ends. */
  bodyEnd :Int
) extends KeyedEntity[Long] {
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0L, 0L, 0L, "", 0, 0, 0, 0, 0, 0, 0)

  override def toString = ("[id=" + id + ", oid=" + outerId + ", uid=" + unitId +
                           ", name=" + name + ", kind=" + kind + ", start=" + defStart + "]")
}

/** Contains metadata for a use. */
case class Use (
  /** The id of the compunit in which this use appears. */
  unitId :Long,
  /** The id of the immediately enclosing definition in which this use occurs. */
  ownerId :Long,
  /** The id of the definition of the referent of this use. */
  referentId :Long,
  /** The kind of the referent of this use. */
  kind :Int,
  /** The location in the source file of the start of this use. */
  useStart :Int,
  /** The location in the source file of the end of this use. */
  useEnd :Int
) {
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0L, 0L, 0, 0, 0)

  override def toString = "[owner=" + ownerId + ", ref=" + referentId + "]"
}

/** Contains information for a def's signature. */
case class Sig (
  /** The id of the def for whom we provide signature data. */
  defId :Long,
  /** The text of the signature. */
  @Column(length=1024) text :String,
  /** The binary data for this signature's defs. */
  defs :Array[Byte],
  /** The binary data for this signature's uses. */
  uses :Array[Byte]
) {
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", null, null)

  override def toString = defId + ": " + text + " (" + defs.length + ", " + uses.length + ")"
}

/** Contains information for a def's documentation. */
case class Doc (
  /** The id of the def for whom we provide documentation. */
  defId :Long,
  /** The text of the documentation. */
  @Column(length=32768) text :String, // TODO: use DefInfo.MAX_DOC_LENGTH
  /** The binary data for uses that occur in the docs. */
  uses :Array[Byte]
) {
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", null)

  override def toString = defId + ": " + text + " (" + uses.length + ")"
}

/** Maintains a mapping from type to supertype. */
case class Super (
  /** The id of the def in question. */
  defId :Long,
  /** The id of (one of) this def's supertype(s). */
  superId :Long
) extends KeyedEntity[CompositeKey2[Long,Long]] {
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0L)

  /** Defines our primary key. */
  def id = compositeKey(defId, superId)

  override def toString = defId + " -> " + superId
}
