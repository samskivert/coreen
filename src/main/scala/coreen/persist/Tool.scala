//
// $Id$

package coreen.persist

import java.io.File
import java.sql.Connection
import java.sql.DriverManager

import org.squeryl.PrimitiveTypeMode._

import coreen.server.{DirsComponent, LogComponent}

/**
 * A tool for manual schema management. Ugh.
 */
object Tool extends AnyRef
  with LogComponent with DirsComponent with DBComponent
{
  def main (args :Array[String]) :Unit = try {
    Class.forName("org.h2.Driver") // initialize the H2 database
    args match {
      case Array("list") => listTables
      case Array("dump", table) => dumpTable(table)
      case Array("schema") => printSchema()
    }
  } catch {
    case _ :MatchError | _ :NumberFormatException =>
      error("Usage: dbtool { list | dump table | schema }")
  }

  protected def listTables {
    execute("show tables")
  }

  protected def dumpTable (table :String) {
    execute("select * from " + table)
  }

  protected def printSchema () {
    initComponents
    startComponents
    transaction {
      _db.printDdl(println(_))
    }
    shutdownComponents
  }

  protected def withConnection (action :(Connection => Unit)) {
    val dburl = "jdbc:h2:" + new File(_coreenDir, "repository").getAbsolutePath
    val conn = DriverManager.getConnection(dburl, "sa", "")
    try {
      action(conn)
    } finally {
      conn.close
    }
  }

  protected def execute (sql :String) {
    withConnection { conn =>
      val stmt = conn.createStatement
      val rs = stmt.executeQuery(sql)
      val md = rs.getMetaData
      val cnames = (1 to md.getColumnCount) map(md.getColumnName)
      var cdata = Vector[Seq[String]]()
      while (rs.next) {
        cdata = (cdata :+ ((1 to md.getColumnCount) map(rs.getObject) map(_.toString)))
      }

      // format the results in a nice grid
      val fmt = format((0 until cnames.length) map(
        i => (cnames(i) +: cdata.map(_(i))) map(_.length) reduceLeft(math.max))) _
      println(fmt(cnames))
      println(fmt(cnames).replaceAll("[^ ]", "-"))
      println(cdata.map(fmt).mkString("\n"))
    }
  }

  protected def format (widths :Seq[Int])(data :Seq[String]) =
    data.zip(widths).map(p => String.format("%-" + p._2 + "s", p._1)).mkString(" ")

  protected def error (msg :String) {
    println(msg)
    System.exit(255)
  }
}
