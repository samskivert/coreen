//
// $Id$

package coreen.persist

import java.io.{File, FileWriter, PrintWriter}
import java.sql.DriverManager
import java.util.Date

import scala.io.Source

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.Query
import org.squeryl.adapters.H2Adapter
import org.squeryl.{Schema, Session, SessionFactory}

import coreen.model.{Kind, DefId, DefDetail, Convert, Def => JDef, CompUnit => JCompUnit}
import coreen.server.{Dirs, Log, Component}

/** Provides database services. */
trait DB {
  /** Defines our database schemas. */
  object _db extends Schema {
    /** The schema version for amazing super primitive migration management system. */
    val version = 9;

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
      d.outerId is(indexed),
      d.name is (indexed, dbType("varchar_ignorecase"))
    )}

    /** A mapping from fully qualfied def name to id (and vice versa). */
    val defmap = table[DefName]
    on(defmap) { dn => declare(
      dn.fqName is(indexed, unique)
    )}

    /** Provides access to the uses table. */
    val uses = table[Use]
    on(uses) { u => declare(
      u.unitId is(indexed),
      u.ownerId is(indexed),
      u.referentId is(indexed)
    )}

    /** Provides access to the supers table. */
    val supers = manyToManyRelation(defs, defs).via[Super](
      (dd, ss, s) => (s.defId === dd.id, s.superId === ss.id))
    on(supers) { s => declare(
      s.superId is(indexed)
    )}
    supers.leftForeignKeyDeclaration.constrainReference(onDelete cascade)
    supers.rightForeignKeyDeclaration.constrainReference(onDelete cascade)

    /** Returns a query that yields all modules in the specified project. */
    def loadModules (projectId :Long) :Query[Def] =
      from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === projectId and cu.id === d.unitId and
              (d.kind === Decode.kindToCode(Kind.MODULE)))
        select(d))

    /** Returns a mapping from fqName to id for all known values in the supplied fqName set. */
    def loadDefIds (fqNames :scala.collection.Set[String]) :Map[String,Long] =
      defmap.where(dn => dn.fqName in fqNames) map(dn => (dn.fqName, dn.id)) toMap

    /** Returns a mapping from fqName to id for all known values in the supplied id set. */
    def loadDefNames (ids :scala.collection.Set[Long]) :Map[String,Long] =
      defmap.where(dn => dn.id in ids) map(dn => (dn.fqName, dn.id)) toMap

    /** Resolves the details for a collection of search matches. */
    def resolveMatches[DD <: DefDetail] (matches :Seq[Def], createDD :() => DD)
                                        (implicit m :ClassManifest[DD]) :Array[DD] = {
      val unitMap = from(_db.compunits)(cu =>
        where(cu.id in matches.map(_.unitId).toSet) select(cu.id, cu.projectId)) toMap

      def mapped (defs :Seq[Def]) = defs map(m => (m.id -> m)) toMap
      def parents (defs :Seq[Def]) =  defs map(_.outerId) filter(0.!=) toSet
      def resolveDefs (have :Map[Long, Def], want :Set[Long]) :Map[Long, Def] = {
        val need = want -- have.keySet
        if (need.isEmpty) have
        else {
          val more = _db.defs.where(d => d.id in need).toArray
          resolveDefs(have ++ mapped(more), parents(more))
        }
      }

      val defMap = resolveDefs(mapped(matches), parents(matches))
      def mkPath (d :Option[Def], path :List[DefId]) :Array[DefId] = d match {
        case None => path.toArray
        case Some(d) => mkPath(defMap.get(d.outerId), Convert.toDefId(d) :: path)
      }

      // sanity check the results and warn about matches for which we have no comp unit
      val (have, missing) = matches partition(d => unitMap.isDefinedAt(d.unitId))
      if (!missing.isEmpty) {
        println("Missing compunits for " + missing)
      }

      have map { d =>
        val pid = unitMap(d.unitId)
        val r = Convert.initDefInfo(d, createDD())
        r.unit = new JCompUnit(d.unitId, pid, null)
        r.path = mkPath(defMap.get(d.outerId), List())
        r
      } toArray
    }

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

    // read the DB version file
    val vfile = new File(_coreenDir, "dbvers.txt")
    val overs = try {
      Source.fromFile(vfile).getLines.next.toInt
    } catch {
      case e => 0
    }
    if (_db.version < overs) {
      _log.warning("DB on file system is higher version than code? Beware.",
                   "file", overs, "code", _db.version)
    }

    // initialize the H2 database
    Class.forName("org.h2.Driver")
    val dburl = _db.dbUrl(_coreenDir)
    SessionFactory.concreteFactory = Some(() => {
      // TODO: use connection pools as Squeryl creates and closes a connection on every query
      val sess = Session.create(DriverManager.getConnection(dburl, "sa", ""), new H2Adapter)
      sess.setLogger(dblogger)
      sess
    })

    // handles migrations
    def writeVersion (version :Int) {
      val out = new PrintWriter(new FileWriter(vfile))
      out.println(version)
      out.close
    }
    def migrate (version :Int, descrip :String, sqls :Seq[String]) {
      if (overs < version) transaction {
        _log.info(descrip)

        // perform the migration
        val stmt = Session.currentSession.connection.createStatement
        try sqls foreach { stmt.executeUpdate(_) }
        finally stmt.close

        // note that we're consistent with the specified version
        writeVersion(version);
      }
    }

    // if we have no version string, we need to initialize the database
    if (overs < 1) {
      _log.info("Initializing schema. [vers=" + _db.version + "]")
      transaction { _db.reinitSchema }
      writeVersion(_db.version)

    } else { // otherwise do migration(s)
      migrate(2, "Adding column Def.flavor...",
              List("alter table Def add column flavor INTEGER(10) not null default 0"))
      migrate(3, "Adding column Def.flags...",
              List("alter table Def add column flags INTEGER(10) not null default 0"))
      migrate(4, "Changing Def.parentId to Def.outerId...",
              List("alter table Def alter column parentId rename to outerId"))
      migrate(5, "Adding Super table...",
              List("create table Super (defId bigint not null, superId bigint not null)",
                   "create index superIdx on Super (superId)",
                   "alter table Super add constraint SuperFK1 foreign key (defId)" +
                   "  references Def(id) on delete cascade",
                   "alter table Super add constraint SuperFK2 foreign key (superId)" +
                   "  references Def(id) on delete cascade",
                   "alter table Super add constraint SuperCPK unique(defId,superId)"))
      migrate(6, "Adding column Def.superId...",
              List("alter table Def add column superId BIGINT not null default 0"))
      migrate(7, "Adding column Project.readerOpts...",
              List("alter table Project add column readerOpts varchar(123)"))
      migrate(8, "Changing Def.flavor to Def.kind...",
              List("alter table Def alter column flavor rename to kind"))
      migrate(9, "Changing Def.kind to Def.flavor and Def.typ to Def.kind...",
              List("alter table Def alter column kind rename to flavor",
                   "alter table Def alter column typ rename to kind"))
    }
  }
}
