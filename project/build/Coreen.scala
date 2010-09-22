import java.io.File
import sbt._

class Coreen (info :ProjectInfo) extends DefaultProject(info) {
  // need our local repository for gwt-utils snapshot
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  // general depends
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"

  // HTTP and GWT depends
  val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
  val gwtUtils = "com.threerings" % "gwt-utils" % "1.1-SNAPSHOT"
  val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25"

  // we don't want these on any of our classpaths, so we make them "system" deps
  val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4" % "system"
  val gwtServlet = "com.google.gwt" % "gwt-servlet" % "2.0.4" % "system"
  val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0" % "system"

  // database depends
  val h2db = "com.h2database" % "h2" % "1.2.142"
  val squeryl = "org.squeryl" % "squeryl_2.8.0" % "0.9.4-RC1"

  // depends for our auto-updating client
  val getdown = "com.threerings" % "getdown" % "1.1-SNAPSHOT"

  // used to obtain the path for a specific dependency jar file
  def depPath (name :String) = managedDependencyRootPath ** (name+"*")

  // generates FooServiceAsync classes from FooService classes for GWT RPC
  lazy val genasync = runTask(Some("com.samskivert.asyncgen.AsyncGenTool"),
                              compileClasspath +++ depPath("gwt-asyncgen"),
                              (mainJavaSourcePath ** "*Service.java" getPaths).toList)

  // generates FooMessages.java from FooMessages.properties for GWT i18n
  lazy val i18nsync = runTask(Some("com.threerings.gwt.tools.I18nSync"), compileClasspath,
                              mainJavaSourcePath.absolutePath :: (
                                mainJavaSourcePath ** "*Messages.properties" getPaths).toList)

  // compiles our GWT client
  lazy val gwtc = runTask(
    Some("com.google.gwt.dev.Compiler"),
    compileClasspath +++ depPath("gwt-dev") +++ mainJavaSourcePath +++ mainResourcesPath,
    List("-war", "target/scala_2.8.0/gwtc", "coreen")) dependsOn(copyResources)

  // packages the output of our GWT client into a jar file
  def packageGwtJar = outputPath / "coreen-gwt.jar"
  lazy val gwtjar = packageTask(mainResources +++ (outputPath / "gwtc" ##) ** "*",
                                packageGwtJar, Nil) dependsOn(gwtc)

  // we include our resources in the -gwt.jar so we exclude them from the main jar
  override def packagePaths = super.packagePaths --- mainResources

  // regenerate our i18n classes every time we compile
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
      compile, copyResources, copyWarResourcesAction)
  }

  // TODO: is there a less hacky way to get this?
  def scalaLibraryPath = "project" / "boot" / "scala-2.8.0" / "lib" / "scala-library.jar"

  // copies the necessary files into place for our Getdown client
  def clientPath = ("client" :Path)
  def clientCodePath = clientPath / "code"
  lazy val prepclient = task {
    // clean out any previous code bits
    FileUtilities.clean(clientCodePath, log)

    // copy all of the appropriate jars into the target directory
    val clientJars = managedClasspath(Configurations.Compile) +++ scalaLibraryPath +++
      depPath("gwt-servlet") --- depPath("gwt-user") --- depPath("gwt-utils")
    FileUtilities.copyFlat(clientJars.get, clientCodePath, log)
    FileUtilities.copyFlat(jarPath.get, clientCodePath, log)
    FileUtilities.copyFlat(packageGwtJar.get, clientCodePath, log)

    // sanitize coreen.jar and getdown.jar, version numbers just get in the way of patching
    def sanitize (jar :File) = {
      val sname = jar.getName.replaceAll("""(_2.\d+.\d+)?-\d+.\d+(-SNAPSHOT)?""", "")
      jar.renameTo(new File(jar.getParentFile, sname))
    }
    ((clientCodePath ** "coreen_*.jar") +++
     (clientCodePath ** "getdown-*.jar")).get foreach(f => sanitize(f.asFile))

    None
  }
  lazy val digest = runTask(Some("com.threerings.getdown.tools.Digester"),
                                  compileClasspath, List("client"))
  lazy val client =  task { None } dependsOn(packageAction, gwtjar, prepclient, digest)
}
