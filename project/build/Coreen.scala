import sbt._

class Coreen (info :ProjectInfo) extends DefaultProject(info) {
  // this project only exists to download gwt-dev, but keep it out of our normal classpaths
  lazy val gdevmode = project("gdevmode", "GWT Dev Mode", new DefaultProject(_) {
    val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4"
  })

  // need our local repository for gwt-utils snapshot
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  // general depends
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"

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

  // run our generators every time we compile
  override def compileAction = super.compileAction dependsOn(i18nsync)

  // to cooperate nicely with GWT devmode when we run the server from within SBT, we copy (not
  // sync) all of our resources to a target/../war directory and remove target/../resources to
  // avoid seeing everything twice
  def warResourcesOutputPath = outputPath / "war"
  def copyWarResourcesAction = copyTask(mainResources, warResourcesOutputPath)
  override def runClasspath =
    super.runClasspath --- mainResourcesOutputPath +++ warResourcesOutputPath
  override protected def runAction = task { args =>
    runTask(getMainClass(true), runClasspath, args) dependsOn(
      compile, copyResources) dependsOn(copyWarResourcesAction)
  }
}
