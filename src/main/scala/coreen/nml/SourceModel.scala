//
// $Id$

package coreen.nml

import scala.xml.{Node, NodeSeq, Elem}

import coreen.model.{Def, Use}

/**
 * Models a source file as a nested collection of definitions and uses.
 */
object SourceModel
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

  /** Models a compilation unit. */
  case class CompUnitElem (var src :String, defs :Seq[DefElem]) {
    def getDef (path :String) :Option[DefElem] = getDef(path split("\\.") toList)
    def getDef (path :List[String]) :Option[DefElem] = defs flatMap(_.getDef(path)) headOption
  }

  /** Models a definition (e.g. class, field, function, method, variable). */
  case class DefElem (name :String, id :String, sig :String, typ :Def.Type,
                      defs :Seq[DefElem], uses :Seq[UseElem], start :Int) extends Span {
    def getDef (path :List[String]) :Option[DefElem] = path match {
      case h :: Nil => if (h == name) Some(this) else None
      case h :: t => if (h == name) defs flatMap(_.getDef(t)) headOption else None
      case _ => None
    }

    def toEdits (source :String) :Seq[Edit] =
      defs.flatMap(_.toEdits(source)) ++ uses.map(_.toEdit(source)) :+ toEdit(source)
  }

  /** Models the use of a name somewhere in a source file. */
  case class UseElem (name :String, target :String, start :Int) extends Span {
    override def toString = "@" + super.toString
  }

  def parse (elem :Elem) :CompUnitElem = {
    assert(elem.size == 1 && elem.head.label == "compunit",
           "DOM must be rooted in a single <compunit> element")
    CompUnitElem(
      (elem \ "@src").text,
      parse0(elem.head.child) filter(_.isInstanceOf[DefElem]) map(_.asInstanceOf[DefElem]))
  }

  // TODO: clean up this ugly hack
  protected def parse0 (elem :NodeSeq) :Seq[AnyRef] = {
    elem map(e => e.label match {
      case "def" => mkDef(e, parse0(e.child))
      case "use" => UseElem((e \ "@name").text, (e \ "@target").text, intAttr(e, "start"))
      case x => null // should only ever be #PCDATA, TODO: assert something to that effect
    })
  }

  protected def mkDef (elem :Node, children :Seq[AnyRef]) :DefElem =
    DefElem((elem \ "@name").text, (elem \ "@id").text, (elem \ "@sig").text, parseType(elem),
            children filter(_.isInstanceOf[DefElem]) map(_.asInstanceOf[DefElem]),
            children filter(_.isInstanceOf[UseElem]) map(_.asInstanceOf[UseElem]),
            intAttr(elem, "start"))

  protected def parseType (elem :Node) = {
    val text = (elem \ "@type").text
    try {
      Enum.valueOf(classOf[Def.Type], text.toUpperCase)
    } catch {
        case e => println(elem + " -> " + e); Def.Type.UNKNOWN
    }
  }

  protected def intAttr (elem :Node, attr :String) = try {
    (elem \ ("@" + attr)).text.toInt
  } catch {
    case e :NumberFormatException => println("Bogus element? " + elem + "@" + attr); 0
  }
}
