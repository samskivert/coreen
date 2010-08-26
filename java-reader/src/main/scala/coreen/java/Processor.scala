//
// $Id$

package coreen.java

import java.util.Set

import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.{
  ProcessingEnvironment, SupportedAnnotationTypes, SupportedSourceVersion}
import javax.tools.Diagnostic
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

import com.sun.source.util.AbstractTypeProcessor
import com.sun.source.util.TreePath
import com.sun.source.util.Trees
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree._

import scalaj.collection.Imports._

/**
 * The main entry point for our Java to NRS javac plugin.
 */
@SupportedAnnotationTypes(Array("*"))
@SupportedSourceVersion(SourceVersion.RELEASE_6)
class Processor extends AbstractTypeProcessor
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

  override def typeProcess (elem :TypeElement, tree :TreePath) {
    println(_scanner(tree.getCompilationUnit.asInstanceOf[JCCompilationUnit]))
  }

  protected var _trees :Trees = _
  protected var _scanner :Scanner = _
}
