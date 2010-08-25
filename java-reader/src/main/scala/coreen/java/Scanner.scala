//
// $Id$

package coreen.java

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
  def scan (tree :JCCompilationUnit) = {
    tree.accept(this)
    _result
  }

  override def visitTopLevel (tree :JCCompilationUnit) {
    val oldunit = _curunit
    _curunit = tree
    super.visitTopLevel(tree)
    _curunit = oldunit
  }

  override def visitClassDef (tree :JCClassDecl) {
    super.visitClassDef(tree)
    _result = <def name={tree.name.toString}
                   start={tree.getStartPosition.toString}
                   end={tree.getEndPosition(_curunit.endPositions).toString}>
      {_result}
    </def>
  }

  protected var _curunit :JCCompilationUnit = _
  protected var _result :Elem = <noop/>
}
