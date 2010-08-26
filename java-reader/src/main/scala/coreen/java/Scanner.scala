//
// $Id$

package coreen.java

import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree._
import com.sun.tools.javac.tree.TreeInfo
import com.sun.tools.javac.tree.TreeScanner
import com.sun.tools.javac.util.Context

import scala.xml.Elem

/**
 * Scans the Java AST and generates NRIF.
 */
object Scanner
{
  def instance (ctx :Context) = {
    var instance = ctx.get(CONTEXT_KEY)
    if (instance == null) {
      instance = new Scanner(ctx)
      ctx.put(CONTEXT_KEY, instance)
    }
    instance
  }

  protected val CONTEXT_KEY = new Context.Key[Scanner]
}

class Scanner (ctx :Context) extends TreeScanner
{
  def apply (tree :JCTree) :Seq[Elem] = {
    scan(tree)
    _result
  }

  override def visitTopLevel (tree :JCCompilationUnit) {
    val oldunit = _curunit
    _curunit = tree
    super.visitTopLevel(tree)
    _curunit = oldunit
  }

  override def visitClassDef (tree :JCClassDecl) {
    _result += <def name={tree.name.toString}
                    start={tree.getStartPosition.toString}
                    end={tree.getEndPosition(_curunit.endPositions).toString}>
                 {capture(super.visitClassDef(tree))}
               </def>
  }

  override def visitVarDef (tree :JCVariableDecl) {
    _result += <def name={tree.name.toString}
                    start={tree.getStartPosition.toString}
                    end={tree.getEndPosition(_curunit.endPositions).toString}>
                 <ref target={tree.sym.`type`.toString}
                      start={tree.vartype.getStartPosition.toString}
                      end={tree.vartype.getEndPosition(_curunit.endPositions).toString}/>
               </def>
  }

  // boy Java's AST walking API makes translating a JCTree into something else cumbersome
  def capture (call : =>Unit) = {
    val oresult = _result
    _result = collection.mutable.ArrayBuffer[Elem]()
    call
    val nresult = _result
    _result = oresult
    nresult
  }

  protected var _curunit :JCCompilationUnit = _
  protected var _result = collection.mutable.ArrayBuffer[Elem]()
}
