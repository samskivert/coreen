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
    val name :String
    val start :Int

    def toEdit (source :String) = {
      val nstart = source.indexOf(name, start)
      assert(nstart != -1, "Unable to find name in source: " + name + "@" + start)
      Edit(nstart, nstart + name.length)
    }

    override def toString = name + ":" + start
  }

  case class Edit (start :Int, end :Int)

  /** Models a definition (e.g. class, field, function, method, variable). */
  case class Def (name :String, id :String, typ :JDef.Type, defs :Seq[Def], uses :Seq[Use],
                  start :Int) extends Span {
    def getDef (path :String) :Option[Def] = getDef(path split("\\.") toList)

    def getDef (path :List[String]) :Option[Def] = path match {
      case h :: Nil => if (h == name) Some(this) else None
      case h :: t => if (h == name) defs flatMap(_.getDef(t)) headOption else None
      case _ => None
    }

    def toEdits (source :String) :Seq[Edit] =
      defs.flatMap(_.toEdits(source)) ++ uses.map(_.toEdit(source)) :+ toEdit(source)
  }

  /** Models the use of a name somewhere in a source file. */
  case class Use (name :String, target :String, start :Int) extends Span {
    override def toString = "@" + super.toString
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
      case "use" => Use((e \ "@name").text, (e \ "@target").text, intAttr(e, "start"))
      case x => null // should only ever be #PCDATA, TODO: assert something to that effect
    })
  }

  protected def mkDef (elem :Node, children :Seq[AnyRef]) :Def =
    Def((elem \ "@name").text, (elem \ "@id").text, parseType(elem),
        children.filter(_.isInstanceOf[Def]).map(_.asInstanceOf[Def]),
        children.filter(_.isInstanceOf[Use]).map(_.asInstanceOf[Use]), intAttr(elem, "start"))

  protected def parseType (elem :Node) = {
    val text = (elem \ "@type").text
    try {
      Enum.valueOf(classOf[JDef.Type], text.toUpperCase)
    } catch {
        case e => println(elem + " -> " + e); JDef.Type.UNKNOWN
    }
  }

  protected def intAttr (elem :Node, attr :String) = try {
    (elem \ ("@" + attr)).text.toInt
  } catch {
    case e :NumberFormatException => println("Bogus element? " + elem + "@" + attr); 0
  }
}
