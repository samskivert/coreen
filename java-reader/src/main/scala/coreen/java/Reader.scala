//
// $Id$

package coreen.java

import java.net.URI

import javax.tools.JavaFileObject
import javax.tools.SimpleJavaFileObject
import javax.tools.ToolProvider

import com.sun.source.tree.ClassTree
import com.sun.source.tree.CompilationUnitTree
import com.sun.source.tree.MethodTree
import com.sun.source.tree.Tree
import com.sun.source.tree.VariableTree
import com.sun.source.util.JavacTask
import com.sun.source.util.TreePathScanner
import com.sun.tools.javac.tree.JCTree._

import scala.xml.Elem
import scala.collection.mutable.ArrayBuffer

import scalaj.collection.Imports._

/**
 * Provides an API for convering Java source to name-resolved source (NRS).
 */
object Reader
{
  def process (filename :String, content :String) :List[Elem] =
    process(List(mkTestObject(filename, content))) head

  def process (files :List[JavaFileObject]) :List[List[Elem]] = {
    val task = compiler.getTask(null, null, null, null, null, files.asJava).asInstanceOf[JavacTask]
    val asts = task.parse.asScala
    task.analyze
    asts map(ast => scanner.scan(ast)) toList
  }

  private def mkTestObject (file :String, content :String) =
    new SimpleJavaFileObject(URI.create("test:/" + file), JavaFileObject.Kind.SOURCE) {
      override def getCharContent (ignoreEncodingErrors :Boolean) :CharSequence = content
    }

  private val scanner = new TreePathScanner[Unit,ArrayBuffer[Elem]] {
    def scan (path :Tree) :List[Elem] = {
      val buf = ArrayBuffer[Elem]()
      scan(path, buf)
      buf toList
    }

    override def visitCompilationUnit (node :CompilationUnitTree, buf :ArrayBuffer[Elem]) {
      val oldunit = _curunit
      _curunit = node.asInstanceOf[JCCompilationUnit]
      super.visitCompilationUnit(node, buf)
      _curunit = oldunit
    }

    override def visitClass (node :ClassTree, buf :ArrayBuffer[Elem]) {
      val tree = node.asInstanceOf[JCClassDecl]
      buf += <def name={tree.name.toString}
                  start={tree.getStartPosition.toString}
                  end={tree.getEndPosition(_curunit.endPositions).toString}>
                 {capture(super.visitClass(node, _))}
               </def>
    }

    override def visitMethod (node :MethodTree, buf :ArrayBuffer[Elem]) {
      val tree = node.asInstanceOf[JCMethodDecl]
      // don't emit a def for synthesized ctors
      if (tree.getStartPosition != tree.getEndPosition(_curunit.endPositions)) {
        buf += <def name={tree.name.toString}
                    start={tree.getStartPosition.toString}
                    end={tree.getEndPosition(_curunit.endPositions).toString}>
                   {capture(super.visitMethod(node, _))}
               </def>
      }
    }

    override def visitVariable (node :VariableTree, buf :ArrayBuffer[Elem]) {
      val tree = node.asInstanceOf[JCVariableDecl]
      buf += <def name={tree.name.toString}
                  start={tree.getStartPosition.toString}
                  end={tree.getEndPosition(_curunit.endPositions).toString}>
               <use target={tree.sym.`type`.toString}
                    start={tree.vartype.getStartPosition.toString}
                    end={tree.vartype.getEndPosition(_curunit.endPositions).toString}/>
             </def>
    }

    protected def capture (call :ArrayBuffer[Elem] => Unit) = {
      val sbuf = ArrayBuffer[Elem]()
      call(sbuf)
      sbuf
    }

    protected var _curunit :JCCompilationUnit = _
  }

  private val compiler = ToolProvider.getSystemJavaCompiler
}
