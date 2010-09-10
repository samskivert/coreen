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
    // we need GWT user and dev for compiling the GWT bits and for devmode
    val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
    val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4"

    val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25"

    // add a custom Ivy repository for gwt-asyncgen
    val ivyPattern = "[organization]/[module]/[revision]/[type]s/[artifact].[ext]"
    val gwtAsyncGenRepo = Resolver.url("gwt-asyncgen").artifacts(
      "http://gwt-asyncgen.googlecode.com/svn/releases/" + ivyPattern)
    val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0"

    // generates FooServiceAsync classes from FooService classes for GWT RPC
    def gentask (svcs :PathFinder) = {
      val sources = svcs.getRelativePaths.map(_.replaceAll("Service.java", "ServiceAsync.java"))
      val sourcePaths = sources.map(f => Path.fromString(info.projectPath, f))
      fileTask(sourcePaths from svcs) {
        defaultRunner.run("com.samskivert.asyncgen.AsyncGenTool", compileClasspath.get,
                          svcs.getPaths.toList, log)
      }
    }
    lazy val genasync = gentask(mainJavaSourcePath ** "*Service.java")

    // generates FooMessages.java from FooMessages.properties for GWT i18n
    def i18ntask (props :PathFinder) = {
      val sources = props.getRelativePaths.map(_.replaceAll(".properties$", ".java"))
      val sourcePaths = sources.map(f => Path.fromString(info.projectPath, f))
      fileTask(sourcePaths from props) {
        defaultRunner.run("com.threerings.gwt.tools.I18nSync", compileClasspath.get,
                          mainJavaSourcePath.absolutePath :: props.getPaths.toList, log)
      }
    }
    lazy val i18nsync = i18ntask(mainJavaSourcePath ** "*Messages.properties")

    override def compileAction = super.compileAction dependsOn(i18nsync) dependsOn(genasync)
  }, util, javaReader)

  lazy val javaReader = project("java-reader", "Java Reader", new DefaultProject(_) {
    // nothing special yet
  }, util)
}
