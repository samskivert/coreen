//
// $Id$

package coreen.persist

import java.io.File
import java.sql.DriverManager
import java.util.Date

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl.annotations.Column
import org.squeryl.{KeyedEntity, Schema, Session, SessionFactory}

import coreen.model.{Def => JDef}
import coreen.server.{Dirs, Log, Component}

/** Provides database services. */
trait DB {
  /** Defines our database schemas. */
  object _db extends Schema {
    /** Provides access to the projects table. */
    val projects = table[Project]

    /** Provides access to the compilation units table. */
    val compunits = table[CompUnit]
    on(compunits) { cu => declare(
      cu.path is(indexed)
    )}

    /** Provides access to the defs table. */
    val defs = table[Def]
    on(defs) { d => declare(
      // this index on 'id' magically overrides the primary key index and allows us to insert
      // defs without having a new id assigned to them
      d.id is(indexed),
      d.unitId is(indexed),
      d.parentId is(indexed),
      d.name is (indexed, dbType("varchar_ignorecase"))
    )}

    /** A mapping from fully qualfied def name to id (and vice versa). */
    val defmap = table[DefName]
    on(defmap) { dn => declare(
      dn.fqName is(indexed, unique)
    )}

    /** Returns a mapping from fqName to id for all known values in the supplied fqName set. */
    def loadDefIds (fqNames :scala.collection.Set[String]) :Map[String,Long] =
      defmap.where(dn => dn.fqName in fqNames) map(dn => (dn.fqName, dn.id)) toMap

    /** Returns a mapping from fqName to id for all known values in the supplied id set. */
    def loadDefNames (ids :scala.collection.Set[Long]) :Map[String,Long] =
      defmap.where(dn => dn.id in ids) map(dn => (dn.fqName, dn.id)) toMap

    /** Provides access to the uses table. */
    val uses = table[Use]
    on(uses) { u => declare(
      u.unitId is(indexed),
      u.ownerId is(indexed),
      u.referentId is(indexed)
    )}

    /** Maps {@link JDef.Type} elements to a byte that can be used in the DB. */
    val typeToCode = Map(
      // these mappings must never change
      JDef.Type.MODULE -> 1,
      JDef.Type.TYPE -> 2,
      JDef.Type.FUNC -> 3,
      JDef.Type.TERM -> 4,
      JDef.Type.UNKNOWN -> 0
    )

    /** Maps a byte code back to a {@link JDef.Type}. */
    val codeToType = typeToCode map { case(x, y) => (y, x) }

    /** Creates the JDBC URL to our database. */
    def dbUrl (root :File) =
      "jdbc:h2:" + new File(root, "repository").getAbsolutePath

    /** Drops all tables and recreates the schema. Annoyingly this is the only sort of "migration"
     * supported by Squeryl. */
    def reinitSchema {
      drop
      create
    }
  }
}

/** A concrete implementation of {@link DB}. */
trait DBComponent extends Component with DB {
  this :Log with Dirs =>

  /** Mixer can override this to log database queries. */
  protected def dblogger :(String => Unit) = null

  override protected def initComponents {
    super.initComponents

    // initialize the H2 database
    Class.forName("org.h2.Driver")
    val dburl = _db.dbUrl(_coreenDir)
    SessionFactory.concreteFactory = Some(() => {
      // TODO: use connection pools as Squeryl creates and closes a connection on every query
      val sess = Session.create(DriverManager.getConnection(dburl, "sa", ""), new H2Adapter)
      sess.setLogger(dblogger)
      sess
    })

    // TODO: squeryl doesn't support any sort of schema migration; sigh
    if (_firstTime) transaction { _db.reinitSchema }
  }
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
  /** When this project was imported into the library. */
  imported :Long,
  /** When this project was last updated. */
  lastUpdated :Long
) extends KeyedEntity[Long] {
  /* ctor */ { assert(!rootPath.endsWith("/")) }

  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this("", "", "", Some(""), 0L, 0L)

  override def toString = "[id=" + id + ", name=" + name + ", vers=" + version + "]"
}

/** Contains metadata for a single compilation unit. */
case class CompUnit (
  /** The id of the project to which this compilation unit belongs. */
  projectId :Long,
  /** The path (relative to the project root) to this compilation unit. */
  path :String,
  /** The time at which this compilation unit was last updated. */
  lastUpdated :Long
) extends KeyedEntity[Long] {
  /** A unique identifier for this project (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", 0L)

  override def toString = "[id=" + id + ", pid=" + projectId + ", path=" + path + "]"
}

/** A mapping from fully qualified name to id for defs. */
case class DefName (
  /** The fully qualified name of this def. */
  @Column(length=1024) fqName :String
) extends KeyedEntity[Long] {
  /** A unique identifier for this definition (1 or higher). */
  val id :Long = 0L
}

/** Contains metadata for a definition. */
case class Def (
  /** A unique identifier for this definition (1 or higher). */
  id :Long,
  /** The id of this definition's enclosing definition, or 0 if none. */
  parentId :Long,
  /** The id of this definition's enclosing compunit. */
  unitId :Long,
  /** This definition's (unqualified) name (i.e. Foo not com.bar.Outer.Foo). */
  name :String,
  /** The nature of this definition (function, term, etc.). See {@link JDef.Type}. */
  typ :Int,
  /** This definition's (type) signature. */
  @Column(length=1024) sig :Option[String],
  /** This definition's documentation. */
  @Column(length=32768) doc :Option[String],
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
  def this () = this(0L, 0L, 0L, "", 0, Some(""), Some(""), 0, 0, 0, 0)

  override def toString = ("[id=" + id + ", pid=" + parentId + ", uid=" + unitId +
                           ", name=" + name + ", type=" + typ + "]")
}

/** Contains metadata for a use. */
case class Use (
  /** The id of the compunit in which this use appears. */
  unitId :Long,

  /** The id of the immediately enclosing definition in which this use occurs. */
  ownerId :Long,

  /** The id of the definition of the referent of this use. */
  referentId :Long,

  /** The location in the source file of the start of this use. */
  useStart :Int,

  /** The location in the source file of the end of this use. */
  useEnd :Int
) {
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0L, 0L, 0, 0)

  override def toString = ("[owner=" + ownerId + ", ref=" + referentId + "]")
}
