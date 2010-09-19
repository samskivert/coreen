//
// $Id$

package coreen.persist

import java.util.Date

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Schema
import org.squeryl.KeyedEntity

/**
 * Handles our persistence needs.
 */
object DB extends Schema
{
  /** Provides access to the projects repository. */
  val projects = table[Project]

  /** Provides access to the compilation units repository. */
  val compunits = table[CompUnit]

  /** Drops all tables and recreates the schema. Annoyingly this is the only sort of "migration"
   * supported by Squeryl. */
  def reinitSchema {
    drop
    create
  }
}

/** Contains project metadata. */
case class Project (
  /** The (human readable) name of this project. */
  val name :String,
  /** The path to the root of this project. */
  val rootPath :String,
  /** A string identifying the imported version of this project. */
  val version :String,
  /** When this project was imported into the library. */
  val imported :Long,
  /** When this project was last updated. */
  val lastUpdated :Long) extends KeyedEntity[Long]
{
  /* ctor */ { assert(!rootPath.endsWith("/")) }

  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this("", "", "", 0L, 0L)

  override def toString = "[id=" + id + ", name=" + name + ", vers=" + version + "]"
}

/** Contains metadata for a single compilation unit. */
case class CompUnit (
  /** The id of the project to which this compilation unit belongs. */
  val projectId :Long,
  /** The path (relative to the project root) to this compilation unit. */
  val path :String,
  /** The time at which this compilation unit was last updated. */
  val lastUpdated :Long)
{
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", 0L)

  override def toString = projectId + ":" + path
}
