import java.io.{File, FileInputStream}
import sbt._
import net.ps.github.Uploader

class Coreen (info :ProjectInfo) extends DefaultProject(info) with ProguardProject {
  // need our local repository for gwt-utils snapshot
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  // general depends
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"

  // HTTP and GWT depends
  val gwtUser = "com.google.gwt" % "gwt-user" % "2.1.0"
  val gwtUtils = "com.threerings" % "gwt-utils" % "1.2-SNAPSHOT"
  val jetty = "org.mortbay.jetty" % "jetty" % "6.1.25"

  // we don't want these on any of our classpaths, so we make them "system" deps
  val gwtDev = "com.google.gwt" % "gwt-dev" % "2.1.0" % "system"
  val gwtServlet = "com.google.gwt" % "gwt-servlet" % "2.1.0" % "system"
  val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0" % "system"

  // database depends
  val h2db = "com.h2database" % "h2" % "1.2.142"
  val squeryl = "org.squeryl" % "squeryl_2.8.0" % "0.9.4-RC3"
  val neo4jKernel = "org.neo4j" % "neo4j-kernel" % "1.2-1.2.M03"

  // depends for our auto-updating client
  val getdown = "com.threerings" % "getdown" % "1.1-SNAPSHOT"

  // pass some useful arguments to javac
  override def javaCompileOptions = List(
    JavaCompileOption("-Xlint:all"), JavaCompileOption("-Xlint:-serial")
  ) ++ super.javaCompileOptions

  // specify our main class
  override def mainClass = Some("coreen.server.Coreen")

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

  // our GWT client code gets compiled as part of the standard build, but we don't really want that
  // code packaged with our server jar as it causes Proguard to link in a bunch of useless GWT crap
  // from gwt-servlet; so we prune said code out prior to building our jar
  lazy val pruneClient = task {
    val srcroot = mainJavaSourcePath.asFile.getPath
    val destdir = mainCompilePath.asFile
    (mainJavaSourcePath ** "*.java").get map(_.asFile) foreach { src =>
      if (!src.getPath.matches(".*coreen/(model|rpc|persist).*")) {
        val cldir = new File(destdir, src.getParent.substring(srcroot.length))
        FileUtilities.clean(
          (Path.fromFile(cldir) ** src.getName.replaceAll(".java", "*")).get, true, log)
      }
    }
    None
  }
  override def packageAction = pruneClient && super.packageAction

  // runs our various tools with args
  lazy val ptool = task { args =>
    runTask(Some("coreen.project.Tool"), runClasspath, args) dependsOn(compile)
  }
  lazy val dbtool = task { args =>
    runTask(Some("coreen.persist.Tool"), runClasspath, args) dependsOn(compile)
  }

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

  // proguard plugin configurations
  override def proguardInJars = super.proguardInJars +++ scalaLibraryPath +++
    depPath("gwt-servlet") --- depPath("gwt-user") --- depPath("gwt-utils")
  override def proguardOptions = List(
    "-dontnote !coreen.**",
    "-keep class coreen.** { *; }",
    "-keep class com.google.gwt.** { *; }",
    "-keep class org.squeryl.** { *; }",
    "-keep class net.sf.cglib.** { *; }",
    "-keep class org.neo4j.kernel.** { *; }"
  )

  // copies the necessary files into place for our Getdown client
  def clientOutPath = outputPath / "client"
  def javaReaderJarPath = "java-reader" / "target" / "scala_2.8.0" ** "coreen-java-reader_*.min.jar"
  lazy val prepclient = task {
    // clean out any previous bits
    FileUtilities.clean(clientOutPath, log)

    // copy our stock metadata
    FileUtilities.copyFlat(("client" / "getdown" * "*").get, clientOutPath, log)

    // copy all of the appropriate jars into the target directory
    FileUtilities.copyFlat(minJarPath.get, clientOutPath, log)
    FileUtilities.copyFlat(packageGwtJar.get, clientOutPath, log)
    FileUtilities.copyFlat(depPath("getdown").get, clientOutPath, log)
    FileUtilities.copyFlat(javaReaderJarPath.get, clientOutPath, log)

    // sanitize our project jar files, version numbers will get in the way of patching
    def sanitize (jar :File) = {
      val sname = jar.getName.replaceAll("""(_2.\d+.\d+)?-\d+.\d+(-SNAPSHOT)?(.min)?""", "")
      jar.renameTo(new File(jar.getParentFile, sname))
    }
    (clientOutPath ** "*.jar").get foreach(f => sanitize(f.asFile))

    None
  }
  lazy val digest = runTask(Some("com.threerings.getdown.tools.Digester"),
                            compileClasspath, List(clientOutPath.asFile.getPath))
  lazy val client = packageAction && proguard && gwtjar && prepclient && digest

  lazy val pubclient = task {
    val creds = (Path.userHome / ".github" / "credentials").asFile
    if (!creds.exists) Some("Missing " + creds)
    else {
      val List(login, token) = io.Source.fromFile(creds).getLines.map(_.trim).toList
      val files = (clientOutPath * "*").get.map(_.asFile).toList
      new Uploader(login, token, "coreen") upload(files :_*)
      None
    }
  } dependsOn(client)
}
