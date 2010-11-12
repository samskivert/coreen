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

import org.neo4j.graphdb.{Direction, GraphDatabaseService, Node, Transaction}
import org.neo4j.kernel.EmbeddedGraphDatabase

import coreen.model.{Kind, DefId, DefDetail, Convert, Def => JDef, CompUnit => JCompUnit}
import coreen.server.{Dirs, Log, Component}
import coreen.util.Tree

/** Provides database services. */
trait DB {
  /** Defines our database schemas. */
  object _db extends Schema {
    /** The schema version for amazing super primitive migration management system. */
    val version = 16;

    /** Provides access to the projects table. */
    val projects = table[Project]

    /** Provides access to the compilation units table. */
    val compunits = table[CompUnit]
    on(compunits) { cu => declare(
      cu.path is(indexed)
    )}
    val unitsToProject = oneToManyRelation(projects, compunits).via(
      (p, cu) => p.id === cu.projectId)
    unitsToProject.foreignKeyDeclaration.constrainReference(onDelete cascade)

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
    val defsToUnit = oneToManyRelation(compunits, defs).via(
      (cu, d) => cu.id === d.unitId)
    defsToUnit.foreignKeyDeclaration.constrainReference(onDelete cascade)

    /** Provides access to the uses table. */
    val uses = table[Use]
    on(uses) { u => declare(
      u.unitId is(indexed),
      u.ownerId is(indexed),
      u.referentId is(indexed)
    )}
    val usesToOwner = oneToManyRelation(defs, uses).via(
      (d, u) => d.id === u.ownerId)
    usesToOwner.foreignKeyDeclaration.constrainReference(onDelete cascade)

    /** Provides access to the sigs table. */
    val sigs = table[Sig]
    on(sigs) { s => declare(
      s.defId is(indexed)
    )}
    val defsToSigs = oneToManyRelation(defs, sigs).via((d, s) => d.id === s.defId)
    defsToSigs.foreignKeyDeclaration.constrainReference(onDelete cascade)

    /** Provides access to the docs table. */
    val docs = table[Doc]
    on(docs) { s => declare(
      s.defId is(indexed)
    )}
    val defsToDocs = oneToManyRelation(defs, docs).via((d, s) => d.id === s.defId)
    defsToDocs.foreignKeyDeclaration.constrainReference(onDelete cascade)

    /** Provides access to the supers table. */
    val supers = manyToManyRelation(defs, defs).via[Super](
      (dd, ss, s) => (s.defId === dd.id, s.superId === ss.id))
    on(supers) { s => declare(
      s.superId is(indexed)
    )}
    supers.leftForeignKeyDeclaration.constrainReference(onDelete cascade)
    supers.rightForeignKeyDeclaration.constrainReference(onDelete cascade)

    /** Creates a def map that can be used to resolve names. The intended usage pattern is to
     * create a DefMap for a particular task, caching names while the task is being performed, and
     * then dropping references to the map so that it may be garbage collected. */
    def newDefMap = new DefMap {
      def resolveIds (fqNames :Traversable[String], andChildren :Boolean) {
        val root = new Tree[Boolean]()
        fqNames foreach(n => root.add(n.split(" "), andChildren))
        val tx = _neodb.beginTx
        try {
          resolveChildren(root, _map)
          tx.success
        } finally {
          tx.finish
        }
      }

      def assignIds (fqNames :Traversable[String]) {
        val root = new Tree[Unit]()
        fqNames foreach(n => root.add(n.split(" "), ()))
        val tx = _neodb.beginTx
        try {
          assignChildren(root, _map)
          tx.success
        } finally {
          tx.finish
        }
      }

      def get (fqName :String) = _map.get(fqName.split(" ")) map(_.getId)

      def resolveChildren (node :Tree[Boolean], map :Tree[Node]) {
        import scalaj.collection.Imports._
        node.value match {
          case Some(true) => resolveAllChildren(map)
          case _ => if (!node.children.isEmpty) {
            // resolve any unresolved children
            val toLoad = node.children.keySet -- map.children.keySet
            if (!toLoad.isEmpty) {
              val curnode = map.value.get
              for (nn <- curnode.getRelationships(
                GraphRelations.ENCLOSES, Direction.OUTGOING).asScala) {
                val onode = nn.getOtherNode(curnode)
                val name = onode.getProperty("name").asInstanceOf[String]
                if (toLoad(name)) {
                  map.add(name, onode)
                }
              }
            }
            // now recurse over the children and resolve them
            for (name <- node.children.keySet) {
              val nnext = node(name)
              // we may have nothing left to resolve, or this name node may not exist
              if (map.children.contains(name)) {
                resolveChildren(nnext, map(name))
              }
            }
          }
        }
      }

      def resolveAllChildren (map :Tree[Node]) {
        import scalaj.collection.Imports._
        // TODO: rewrite with a traverser
        if (map.children.isEmpty) {
          val curnode = map.value.get
          for (nn <- curnode.getRelationships(
            GraphRelations.ENCLOSES, Direction.OUTGOING).asScala) {
            val onode = nn.getOtherNode(curnode)
            map.add(onode.getProperty("name").asInstanceOf[String], onode)
          }
          map.children.values.foreach(c => resolveAllChildren(c))
        } // else they're already resolved
      }

      def assignChildren (node :Tree[Unit], map :Tree[Node]) {
        val curnode = map.value.get

        // create nodes for any children we're missing
        for (name <- (node.children.keySet -- map.children.keySet)) {
          val node = _neodb.createNode
          node.setProperty("name", name)
          curnode.createRelationshipTo(node, GraphRelations.ENCLOSES)
          map.add(name, node)
        }

        for ((name, next) <- node.children) {
          if (!next.children.isEmpty) assignChildren(next, map.get(name))
        }
      }

      protected val _map = new Tree(_neodb.getReferenceNode)
    }

    /** Returns a query that yields all modules in the specified project. */
    def loadModules (projectId :Long) :Query[Def] =
      from(_db.compunits, _db.defs)((cu, d) =>
        where(cu.projectId === projectId and cu.id === d.unitId and
              (d.kind === Decode.kindToCode(Kind.MODULE)))
        select(d))

    /** Loads up the signature information for the specified defs. */
    def loadSigs (ids :scala.collection.Set[Long]) :Map[Long, Sig] =
      _db.sigs.where(s => s.defId in ids) map(s => (s.defId -> s)) toMap

    /** Loads up the doc information for the specified defs. */
    def loadDocs (ids :scala.collection.Set[Long]) :Map[Long, Doc] =
      _db.docs.where(d => d.defId in ids) map(d => (d.defId -> d)) toMap

    /** Resolves the details for a collection of search matches. */
    def resolveMatches[DD <: DefDetail] (matches :Seq[Def], createDD :() => DD)
                                        (implicit m :ClassManifest[DD]) :Array[DD] = {
      val unitMap = from(_db.compunits)(cu =>
        where(cu.id in matches.map(_.unitId).toSet) select(cu.id, cu.projectId)) toMap

      def parents (defs :Seq[Def]) =  defs map(_.outerId) filter(0.!=) toSet
      def resolveDefs (have :Map[Long, Def], want :Set[Long]) :Map[Long, Def] = {
        val need = want -- have.keySet
        if (need.isEmpty) have
        else {
          val more = _db.defs.where(d => d.id in need).toArray
          resolveDefs(have ++ mapDefs(more), parents(more))
        }
      }

      val matchMap = mapDefs(matches)
      val defMap = resolveDefs(matchMap, parents(matches))
      def mkPath (d :Option[Def], path :List[DefId]) :Array[DefId] = d match {
        case None => path.toArray
        case Some(d) => mkPath(defMap.get(d.outerId), Convert.toDefId(d) :: path)
      }

      // resolve the signatures and docs for our matches
      val sigs = loadSigs(matchMap.keySet)
      val docs = loadDocs(matchMap.keySet)

      // sanity check the results and warn about matches for which we have no comp unit
      val (have, missing) = matches partition(d => unitMap.isDefinedAt(d.unitId))
      if (!missing.isEmpty) {
        println("Missing compunits for " + missing)
      }

      have map { d =>
        val pid = unitMap(d.unitId)
        val r = Convert.initDefInfo(d, sigs.get(d.id), docs.get(d.id), createDD())
        r.unit = new JCompUnit(d.unitId, pid, null)
        r.path = mkPath(defMap.get(d.outerId), List())
        r
      } toArray
    }

    /** Turns the supplied sequence of defs into a mapping from id to def. */
    def mapDefs (defs :Traversable[Def]) = defs map(m => (m.id -> m)) toMap

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

  /** Provides graph database services, used to maintain (fqName -> id) mapping. */
  val _neodb :GraphDatabaseService
}

/** A concrete implementation of {@link DB}. */
trait DBComponent extends Component with DB {
  this :Log with Dirs =>

  val _neodb :GraphDatabaseService =
    new EmbeddedGraphDatabase(new File(_coreenDir, "neodb").getAbsolutePath)

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
      migrate(10, "Dropping Def.sig...",
              List("alter table Def drop column sig"))
      migrate(11, "Adding Use.kind...",
              List("alter table Use add column kind INTEGER(10) not null default 0"))
      migrate(12, "Creating Sig...",
              List("create table Sig (data binary not null, " +
                   "text varchar(1024) not null, defId bigint not null)",
                   "create index idxSigDefId on Sig (defId)",
                   "alter table Sig add constraint SigFK1 foreign key (defId)" +
                   " references Def(id) on delete cascade"))
      migrate(13, "Changing Sig.data to Sig.uses...",
              List("alter table Sig alter column data rename to uses"))
      migrate(14, "Adding column Sig.defs...",
              List("alter table Sig add column defs binary not null default ''"))
      migrate(15, "Dropping Def.doc...",
              List("alter table Def drop column doc"))
      migrate(16, "Creating Doc...",
              List("create table Doc (uses binary not null, " +
                   "text varchar(32768) not null, defId bigint not null)",
                   "create index idxDocDefId on Doc (defId)",
                   "alter table Doc add constraint DocFK1 foreign key (defId)" +
                   " references Def(id) on delete cascade"))
    }
  }

  override protected def shutdownComponents {
    super.shutdownComponents
    _neodb.shutdown // shut down our neo4j db
  }
}
