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
      val p1in = _db.projects.insert(new Project("Test 1", "/foo/bar/test1", "1.0", None, now, now))

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

      val def1 = Def(25, 1, 1, "One", 1, 1, 0, None, None, 0, 10, 0, 10)
      _db.defs.insert(def1)
      def1.id should equal(25)

      val def2 = Def(99, 25, 1, "Two", 1, 1, 0, None, None, 0, 10, 0, 10)
      val def3 = Def(104, 25, 1, "Three", 1, 1, 0, None, None, 0, 10, 0, 10)
      _db.defs.insert(List(def2, def3))
      def2.id should equal(99)
      def3.id should equal(104)
    }
  }
}
