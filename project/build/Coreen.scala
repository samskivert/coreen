import java.net.URL
import sbt._

class Coreen (info :ProjectInfo) extends ParentProject(info) {
  lazy val util = project("util", "Util", new DefaultProject(_) {
    val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"
    val gwtUtils = "com.threerings" % "gwt-utils" % "1.0-SNAPSHOT"
  })

  lazy val environ = project("environ", "Environment", new DefaultProject(_) {
    val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
    val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25"

//     val gwtServices = "src" / "main" / "java" ** "*Service.java"
//     val gwtAsyncServices = "src" / "main" / "java" ** "*ServiceAsync.java"
//     lazy val genasync = fileTask("genasync", gwtAsyncServices) {
//       com.samskivert.asyncgen.AsyncGenTool.main(gwtServices.map(_.toString).toArray)
//     }

    val javaSourcePath = "src"/"main"/"java"
    def i18ntask (props :PathFinder)(implicit runner: ScalaRun) = {
      val sources = props.getRelativePaths.map(_.replaceAll(".properties$", ".java"))
      val sourcePaths = sources.map(f => Path.fromString(info.projectPath, f))
      fileTask(sourcePaths from props) {
        runner.run("com.threerings.gwt.tools.I18nSync", testClasspath.get,
                   javaSourcePath.toString :: sources.toList, log)
      }
    }
    lazy val i18nsync = i18ntask(javaSourcePath ** "*Messages.properties")

    override def compileAction = super.compileAction dependsOn(i18nsync)
  }, util, javaReader)

  lazy val javaReader = project("java-reader", "Java Reader", new DefaultProject(_) {
    // nothing special yet
  }, util)
}
