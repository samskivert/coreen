import java.net.URL
import sbt._

class Coreen (info :ProjectInfo) extends ParentProject(info) {
  lazy val util = project("util", "Util", new DefaultProject(_) {
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"
  })

  lazy val environ = project("environ", "Environment", new DefaultProject(_) {
    // add a custom Ivy repository for gwt-asyncgen
    val ivyPattern = "[organization]/[module]/[revision]/[type]s/[artifact].[ext]"
    val gwtAsyncGenRepo = Resolver.url("gwt-asyncgen").artifacts(
      "http://gwt-asyncgen.googlecode.com/svn/releases/" + ivyPattern)

    val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
    val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0" % "compile"
  }, util, javaReader)

  lazy val javaReader = project("java-reader", "Java Reader", new DefaultProject(_) {
    // nothing special yet
  }, util)
}
