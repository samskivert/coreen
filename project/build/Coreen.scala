import java.net.URL
import sbt._

class Coreen (info :ProjectInfo) extends ParentProject(info) {
  // this project only exists to download gwt-dev, but keep it out of our normal classpaths
  lazy val gdevmode = project("gdevmode", "GWT Dev Mode", new DefaultProject(_) {
    val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4"
  })

  lazy val environ = project("environ", "Environment", new DefaultProject(_) {
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"

    // need our local repository for gwt-utils snapshot
    val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

    // HTTP and GWT depends
    val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
    val gwtUtils = "com.threerings" % "gwt-utils" % "1.1-SNAPSHOT"
    val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25"

    // database depends
    val h2db = "com.h2database" % "h2" % "1.2.142"
    val squeryl = "org.squeryl" % "squeryl_2.8.0" % "0.9.4-RC1"

    // generates FooServiceAsync classes from FooService classes for GWT RPC
    val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0"
    lazy val genasync = runTask(Some("com.samskivert.asyncgen.AsyncGenTool"), compileClasspath,
                                (mainJavaSourcePath ** "*Service.java" getPaths).toList)

    // generates FooMessages.java from FooMessages.properties for GWT i18n
    lazy val i18nsync = runTask(Some("com.threerings.gwt.tools.I18nSync"), compileClasspath,
                                mainJavaSourcePath.absolutePath :: (
                                  mainJavaSourcePath ** "*Messages.properties" getPaths).toList)

    override def compileAction = super.compileAction dependsOn(i18nsync) dependsOn(genasync)
  }, javaReader)

  lazy val javaReader = project("java-reader", "Java Reader", new DefaultProject(_) {
    val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
    val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"
  })
}
