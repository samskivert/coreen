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
  case class Def (id :Int, name :String, typ :JDef.Type, defs :Seq[Def], bodyStart :Int,
                  uses :Seq[Use], start :Int, end :Int) extends Span {
    def getDef (path :String) :Option[Def] = getDef(path split("\\.") toList)

    def getDef (path :List[String]) :Option[Def] = path match {
      case h :: Nil => if (h == name) Some(this) else None
      case h :: t => if (h == name) defs flatMap(_.getDef(t)) headOption else None
      case _ => None
    }

    override def toString = name
  }

  /** Models the use of a name somewhere in a source file. */
  case class Use (id :Int, target :String, start :Int, end :Int) extends Span {
    override def toString = "@" + target
  }

  def parse (elem :NodeSeq) :Def = {
    assert(elem.size == 1 && elem.head.label == "def",
           "DOM must be rooted in a single <def> element")
    mkDef(elem.head, parse0(elem.head.child))
  }

  // TODO: clean up this ugly hack
  protected def parse0 (elem :NodeSeq) :Seq[AnyRef] = {
    elem map(e => e.label match {
      case "def" => mkDef(e, parse0(e.child))
      case "use" => Use(0, (e \ "@target").text, intAttr(e, "start"), intAttr(e, "end"))
      case x => null // should just be #PCDATA, TODO: assert something useful
    })
  }

  protected def mkDef (elem :Node, children :Seq[AnyRef]) :Def =
    Def(0, (elem \ "@name").text, null,
        children.filter(_.isInstanceOf[Def]).map(_.asInstanceOf[Def]), 0,
        children.filter(_.isInstanceOf[Use]).map(_.asInstanceOf[Use]),
        intAttr(elem, "start"), intAttr(elem, "end"))

  protected def intAttr (elem :Node, attr :String) = try {
    (elem \ ("@" + attr)).text.toInt
  } catch {
    case e :NumberFormatException => println("Bogus element? " + elem + "@" + attr); 0
  }
}
