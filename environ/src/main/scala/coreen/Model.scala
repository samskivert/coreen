//
// $Id$

package coreen

import scala.xml.Node
import scala.xml.NodeSeq

import model.{Def => JDef, Use => JUse}

/**
 * Models a source file as a nested collection of definitions and uses.
 */
object Model
{
  /** Tracks a span of characters in a source file. */
  trait Span {
    val start :Int
    val end :Int
    def length = end - start
  }

  /** Models a definition (e.g. class, field, function, method, variable). */
  case class Def (id :Int, parent :Def, name :String, typ :JDef.Type, defs :Seq[Def],
                  bodyStart :Int, uses :Seq[Use], start :Int, end :Int) extends Span {
    /** Converts this Scala model class into its Java equivalent (for interop with GWT). */
    def toJava :JDef = new JDef(id, parent.toJava, name, typ, defs map(_.toJava) toArray,
                                bodyStart, uses map(_.toJava) toArray, start, end)

    override def toString = name + "(" + defs.mkString(", ") + " // " + uses.mkString(", ") + ")"
  }

  /** Models the use of a name somewhere in a source file. */
  case class Use (id :Int, owner :Def, referent :Def, start :Int, end :Int) extends Span {
    /** Converts this Scala model class into its Java equivalent (for interop with GWT). */
    def toJava :JUse = new JUse(id, owner.toJava, referent.toJava, start, end)
    // override def toString = name
  }

  def parse (elem :NodeSeq) :Seq[AnyRef] = {
    elem map(e => e.label match {
      case "def" => {
        val children = parse(e.child)
        Def(0, null, (e \ "@name").text, null,
            children.filter(_.isInstanceOf[Def]).map(_.asInstanceOf[Def]), 0,
            children.filter(_.isInstanceOf[Use]).map(_.asInstanceOf[Use]),
            intAttr(e, "start"), intAttr(e, "end"))
      }
      case "use" => Use(0, null, null, intAttr(e, "start"), intAttr(e, "end"))
      case x => println("Hrm " + x); null
    })
  }

  protected def intAttr (elem :Node, attr :String) = try {
    (elem \ ("@" + attr)).text.toInt
  } catch {
    case e :NumberFormatException => println("Bogus element? " + elem + "/" + attr); 0
  }
}
