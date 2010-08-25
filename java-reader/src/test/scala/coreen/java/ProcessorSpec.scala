//
// $Id$

package coreen.java;

import java.io.File
import java.net.URLClassLoader
import java.util.Arrays

import javax.tools.{DiagnosticCollector, JavaFileObject, ToolProvider}

import scala.io.Source

import scalaj.collection.Imports._

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Tests the Java to NRIF converting processor.
 */
class ProcessorSpec extends FlatSpec with ShouldMatchers
{
  "Processor" should "do some stuff" in {
    // TODO: this is hideous, can we get this path more easily?
    val cwd = new File(System.getProperty("user.dir"))
    val troot = new File(cwd, "java-reader/target/scala_2.8.0")

    // JavaCompiler does not inherit the classpath of the running JVM, so we sneakily extract the
    // SBT classpath from the classloader that loaded this file
    val ppath = getClass.getClassLoader.asInstanceOf[URLClassLoader].getURLs.
      // and convert the URLs into files
      map(u => new File(u.getPath)).
      // filter out test classes since we want don't want to allow Processor to depend on them
      filterNot(_.getPath().matches(".*/test-(classes|resources)"))
    // full classpath is the above plus the scala runtime jar
    val cpath = ppath :+ new File(cwd, "project/boot/scala-2.8.0/lib/scala-library.jar")

    // set up the compiler and various boilerplate bits
    // val compiler = ToolProvider.getSystemJavaCompiler
    // val diags = new DiagnosticCollector[JavaFileObject]
    // val fmgr = compiler.getStandardFileManager(diags, null, null)
    val options = List("-processor", classOf[Processor].getName,
                       "-classpath", cpath map(_.getPath) mkString(":"))

    // this is the file we'll be running the compiler on
    val tfile = new File(troot, "test-resources/Test.java")
    // val cunits = fmgr.getJavaFileObjects(tfile)

    // create a compilation task and run it
    // val success = compiler.getTask(null, fmgr, diags, options.asJava, null, cunits).call
    // fmgr.close

    // println("Success: " + success)
    // println("Diags: " + diags.getDiagnostics)

    // TEMP: run the compiler in a separate process because of classloader FAIL when running tests
    // from inside SBT
    val proc = new ProcessBuilder((("javac" :: options) :+ tfile.getPath()).asJava).start
    val output = Source.fromInputStream(proc.getInputStream).getLines.mkString("\n")
    val errput = Source.fromInputStream(proc.getErrorStream).getLines.mkString("\n")
    errput should equal("")
    proc.waitFor should equal(0)

    println(output)
  }
}
