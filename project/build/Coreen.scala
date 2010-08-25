import sbt._

class Coreen (info :ProjectInfo) extends ParentProject(info) {
  lazy val javaReader = project("java-reader", "Java Reader", new JavaReader(_))
  class JavaReader (info :ProjectInfo) extends DefaultProject(info) {
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"
  }
}
