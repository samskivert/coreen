//
// $Id$

package coreen.java

import java.util.Set

import javax.annotation.processing.{
  AbstractProcessor, ProcessingEnvironment, RoundEnvironment,
  SupportedAnnotationTypes, SupportedSourceVersion}
import javax.tools.Diagnostic
import javax.lang.model.SourceVersion
import javax.lang.model.element.{Element, PackageElement, TypeElement}

import com.sun.source.util.Trees
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree._

import scalaj.collection.Imports._

/**
 * The main entry point for our Java to NRS javac plugin.
 */
@SupportedAnnotationTypes(Array("*"))
@SupportedSourceVersion(SourceVersion.RELEASE_6)
class Processor extends AbstractProcessor
{
  override def init (procenv :ProcessingEnvironment) {
    super.init(procenv)
    procenv match {
      case jpe :JavacProcessingEnvironment => {
        val ctx = procenv.asInstanceOf[JavacProcessingEnvironment].getContext
        _trees = Trees.instance(procenv)
        _scanner = Scanner.instance(ctx)
        Debug.log("Coreen running", "vers", procenv.getSourceVersion)
      }
      case _ => {
        procenv.getMessager.printMessage(Diagnostic.Kind.WARNING, "Coreen requires javac v1.6.")
      }
    }
  }

  def process (annotations :Set[_ <: TypeElement], roundEnv :RoundEnvironment) :Boolean = {
    // if we were about to initialize ourselves in init() then stop here
    if (_trees == null) return false

    // javac gives us Element which we want to convert to JCompilationUnit (internal API)
    val elems = roundEnv.getRootElements.asScala
    // we get an Element for every top-level class in a compilation unit, but we only want to
    // process each compilation unit once, so we rely on elems being a Set to filter duplicates
    elems map(toUnit) flatten foreach { tree =>
      println(_scanner.scan(tree))
    }
    false
  }

  protected def toUnit (element :Element) =
    Option(_trees.getPath(element)) map(_.getCompilationUnit.asInstanceOf[JCCompilationUnit])

  protected var _trees :Trees = _
  protected var _scanner :Scanner = _
}
