import java.net.URL
import sbt._

class Coreen (info :ProjectInfo) extends ParentProject(info) {
  lazy val util = project("util", "Util", new DefaultProject(_) {
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"
  })

  lazy val environ = project("environ", "Environment", new DefaultProject(_) {
    // TEMP: until our bits are uploaded to Maven Central
    val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

    val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4"
    val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
    val gwtUtils = "com.threerings" % "gwt-utils" % "1.0-SNAPSHOT"
    val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25"

    // generates FooServiceAsync classes from FooService classes for GWT RPC
    val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0"
    lazy val genasync = runTask(Some("com.samskivert.asyncgen.AsyncGenTool"), compileClasspath,
                                (mainJavaSourcePath ** "*Service.java" getPaths).toList)

    // generates FooMessages.java from FooMessages.properties for GWT i18n
    lazy val i18nsync = runTask(Some("com.threerings.gwt.tools.I18nSync"), compileClasspath,
                                mainJavaSourcePath.absolutePath :: (
                                  mainJavaSourcePath ** "*Messages.properties" getPaths).toList)

    override def compileAction = super.compileAction dependsOn(i18nsync) dependsOn(genasync)
  }, util, javaReader)

  lazy val javaReader = project("java-reader", "Java Reader", new DefaultProject(_) {
    // nothing special yet
  }, util)
}
