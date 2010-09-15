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
class DBSpec extends FlatSpec with ShouldMatchers
{
  def testSession = {
    Class.forName("org.h2.Driver")
    Session.create(DriverManager.getConnection("jdbc:h2:mem:test", "sa", ""), new H2Adapter)
  }

  "DB" should "support basic CRUD" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      DB.create

      // create
      val now = System.currentTimeMillis
      val p1in = DB.projects.insert(new Project("Test 1", "/foo/bar/test1", "1.0", now, now))

      // read
      val p1out = DB.projects.lookup(p1in.id).get
      p1out.id should equal(p1in.id)
      p1out.name should equal(p1in.name)
      p1out.rootPath should equal(p1in.rootPath)
      p1out.version should equal(p1in.version)
      p1out.imported should equal(p1in.imported)
      p1out.lastUpdated should equal(p1in.lastUpdated)

      // update
      val later = now + 100
      update(DB.projects) { p =>
        where(p.id === p1in.id)
        set(p.lastUpdated := later)
      }
      val p1out2 = DB.projects.lookup(p1in.id).get
      p1out2.lastUpdated should equal(later)

      // delete
      DB.projects deleteWhere(p => p.id === p1in.id)
      DB.projects.lookup(p1in.id) should equal(None)
    }
  }
}
