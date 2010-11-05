//
// $Id$

package coreen.persist

import java.util.Date
import java.sql.DriverManager

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._

/**
 * Tests the database subsystem.
 */
class DBSpec extends FlatSpec with ShouldMatchers with DB
{
  def testSession = {
    Class.forName("org.h2.Driver")
    val url = "jdbc:h2:mem:test;ignorecase=true"
    val s = Session.create(DriverManager.getConnection(url, "sa", ""), new H2Adapter)
    // s.setLogger(println) // for great debugging!
    s
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

      val def1 = fakeDef(25, 0, 0, 1, "One")
      _db.defs.insert(def1)
      def1.id should equal(25)

      val def2 = fakeDef(99, 25, 0, 1, "Two")
      val def3 = fakeDef(104, 25, 0, 1, "Three")
      _db.defs.insert(List(def2, def3))
      def2.id should equal(99)
      def3.id should equal(104)
    }
  }

  "DB" should "properly obey foreign key constraints for supers" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      _db.reinitSchema

      val def1 = fakeDef(1, 0, 0, 1, "One")
      val def2 = fakeDef(2, 0, 1, 1, "Two")
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

  def fakeDef (id :Long, outerId :Long, superId :Long, unitId :Long, name :String) =
    Def(id, outerId, superId, unitId, name, 1, 1, 0, None, 0, 10, 0, 10)
}
