//
// $Id$

package coreen.persist

import java.io.File
import java.util.Date
import java.sql.DriverManager

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.kernel.EmbeddedGraphDatabase

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._

/**
 * Tests the database subsystem.
 */
class DBSpec extends FlatSpec with BeforeAndAfterAll with ShouldMatchers with DB
{
  def testSession = {
    Class.forName("org.h2.Driver")
    val url = "jdbc:h2:mem:test;ignorecase=true"
    val s = Session.create(DriverManager.getConnection(url, "sa", ""), new H2Adapter)
    // s.setLogger(println) // for great debugging!
    s
  }

  val _neodir = new File("/tmp/neodbtest")
  val _neodb :GraphDatabaseService = new EmbeddedGraphDatabase(_neodir.getAbsolutePath)

  override def afterAll {
    _neodb.shutdown
    recursiveDelete(_neodir)
  }

  def recursiveDelete (file :File) {
    Option(file.listFiles) map(_ map(recursiveDelete))
    file.delete
  }

  "DB" should "support basic CRUD" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      _db.reinitSchema

      // create
      val now = System.currentTimeMillis
      val p1in = _db.projects.insert(
        new Project("Test 1", "/foo/bar/test1", "1.0", None, None, now, now))

      // read
      val p1out = _db.projects.lookup(p1in.id).get
      p1out.id should equal(p1in.id)
      p1out.name should equal(p1in.name)
      p1out.rootPath should equal(p1in.rootPath)
      p1out.version should equal(p1in.version)
      p1out.imported should equal(p1in.imported)
      p1out.lastUpdated should equal(p1in.lastUpdated)

      // update
      val later = now + 100
      update(_db.projects) { p =>
        where(p.id === p1in.id)
        set(p.lastUpdated := later)
      }
      val p1out2 = _db.projects.lookup(p1in.id).get
      p1out2.lastUpdated should equal(later)

      // delete
      _db.projects deleteWhere(p => p.id === p1in.id)
      _db.projects.lookup(p1in.id) should equal(None)
    }
  }

  "DB" should "allow manual specification of id for Def" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      _db.reinitSchema

      val now = System.currentTimeMillis
      val proj = _db.projects.insert(
        Project("Test 1", "/foo/bar/test1", "1.0", None, None, now, now))
      val cu1 = _db.compunits.insert(CompUnit(proj.id, "path", now))

      val def1 = fakeDef(25, 0, 0, cu1.id, "One")
      _db.defs.insert(def1)
      def1.id should equal(25)

      val def2 = fakeDef(99, 25, 0, cu1.id, "Two")
      val def3 = fakeDef(104, 25, 0, cu1.id, "Three")
      _db.defs.insert(List(def2, def3))
      def2.id should equal(99)
      def3.id should equal(104)
    }
  }

  "DB" should "properly obey foreign key constraints for supers" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      _db.reinitSchema

      val now = System.currentTimeMillis
      val proj = _db.projects.insert(
        Project("Test 1", "/foo/bar/test1", "1.0", None, None, now, now))
      val cu1 = _db.compunits.insert(CompUnit(proj.id, "path", now))

      val def1 = fakeDef(1, 0, 0, cu1.id, "One")
      val def2 = fakeDef(2, 0, 1, cu1.id, "Two")
      _db.defs.insert(def1)
      _db.defs.insert(def2)

      // insert a super relationship and make sure it exists
      _db.supers.insert(Super(2, 1))
      _db.supers.left(def2).head should equal(def1)
      _db.supers.right(def1).head should equal(def2)

      // now delete the source def and ensure that the relationship goes away
      _db.defs.delete(1L)
      _db.supers.left(def2).size should equal(0)
      _db.supers.right(def1).size should equal(0)

      // reestablish the relationship
      _db.defs.insert(def1)
      _db.supers.insert(Super(2, 1))
      _db.supers.left(def2).head should equal(def1)
      _db.supers.right(def1).head should equal(def2)

      // now delete the target def and ensure that the relationship goes away
      _db.defs.delete(2L)
      _db.supers.left(def2).size should equal(0)
      _db.supers.right(def1).size should equal(0)
    }
  }

  "DefMap" should "support basic creation and resolution" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      _db.reinitSchema

      val fqNames = List("foo.bar.baz Bif bang", "foo.bar.baz Bif billy",
                         "foo.bar.baz Bing bang", "foo.bar.bif Bazoon binky")

      // to start we should find nothing in the database
      val map = _db.newDefMap
      map.resolveIds(fqNames, false)
      fqNames foreach { name => map.get(name) should equal(None) }

      // now we can assign our ids and should see that they have values
      map.assignIds(fqNames)
      assert(map.get("foo.bar.baz").get >= 0)
      assert(map.get("foo.bar.bif").get >= 0)
      assert(map.get("foo.bar.baz Bif").get >= 0)
      assert(map.get("foo.bar.baz Bif bang").get >= 0)

      // a new map should find these values already in the database
      val nmap = _db.newDefMap
      nmap.resolveIds(fqNames, false)
      assert(nmap.get("foo.bar.baz").get >= 0)
      assert(nmap.get("foo.bar.bif").get >= 0)
      assert(nmap.get("foo.bar.baz Bif").get >= 0)
      assert(nmap.get("foo.bar.baz Bif bang").get >= 0)

      // resolve all children should work
      val nnmap = _db.newDefMap
      nnmap.resolveIds(List("foo.bar.baz"), true)
      assert(nnmap.get("foo.bar.baz").get >= 0)
      assert(nnmap.get("foo.bar.baz Bif").get >= 0)
      assert(nnmap.get("foo.bar.baz Bif bang").get >= 0)
      nnmap.get("foo.bar.bif") should equal(None)

      // // we should also be able to resolve these incrementally
      // val nnmap = _db.newDefMap
      // assert(nnmap.get("foo.bar.baz").get >= 0)
      // assert(nnmap.get("foo.bar.bif").get >= 0)
      // assert(nnmap.get("foo.bar.baz Bif").get >= 0)
      // assert(nnmap.get("foo.bar.baz Bif bang").get >= 0)
    }
  }

  def fakeDef (id :Long, outerId :Long, superId :Long, unitId :Long, name :String) =
    Def(id, outerId, superId, unitId, name, 1, 1, 0, 0, 10, 0, 10)
}
