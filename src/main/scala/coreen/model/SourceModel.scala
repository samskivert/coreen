//
// $Id$

package coreen.model

import scala.xml.{Node, NodeSeq, Elem}

import coreen.model.{Def => JDef}
import coreen.server.Log

/** Provides services for parsing the Coreen intermediate format. */
trait SourceModel {
  this :Log =>

  /** Tracks a span of characters in a source file. */
  trait Span {
    val name :String
    val start :Int

    override def toString = name + ":" + start
  }

  /** Used to validate parsed source model entities. */
  trait Validatable {
    def isValid :Boolean
  }

  /** Models a compilation unit. */
  case class CompUnitElem (var src :String, defs :Seq[DefElem]) extends Validatable {
    def getDef (path :String) :Option[DefElem] = defs flatMap(_.getDef(path)) headOption

    /** Validity requires taht we have a source. */
    def isValid = (src.length > 0)
  }

  /** Models a definition (e.g. class, field, function, method, variable). */
  case class DefElem (
    name :String, id :String, sig :Option[SigElem], doc :Option[DocElem],
    kind :Kind, flavor :Flavor, flags :Int, supers :Seq[String],
    start :Int, bodyStart :Int, bodyEnd :Int, defs :Seq[DefElem], uses :Seq[UseElem]
  ) extends Span with Validatable {
    def getDef (path :String) :Option[DefElem] = {
      if (path == name) Some(this)
      else if (path.startsWith(name + ".")) {
        val rest = path.substring(name.length+1)
        defs flatMap(_.getDef(rest)) headOption
      } else None
    }

    /** Validity requires that we have an id, a name and a source position. */
    def isValid = (name.length > 0) && (id.length > 0) && (start >= 0)
  }

  /** Models the use of a name somewhere in a source file. */
  case class UseElem (
    name :String, target :String, kind :Kind, start :Int
  ) extends Span with Validatable {
    /** Validity requires that we have name, target, and a source position. */
    def isValid = (name.length > 0) && (target.length > 0) && (start >= 0)

    override def toString = "@" + super.toString + " -> " + target
  }

  /** Models the information for a def signature. */
  case class SigElem (text :String, defs :Seq[SigDefElem], uses :Seq[UseElem])

  /** Models limited information on a def for a signature. */
  case class SigDefElem (id :String, name :String, kind :Kind, start :Int) extends Span {
    override def toString = super.toString + ":" + kind
  }

  /** Models the information for a doc signature. */
  case class DocElem (text :String, uses :Seq[UseElem])

  /** Models a source file as a nested collection of definitions and uses. */
  object _model
  {
    /** Flattens all nested defs into a single seq (including those supplied). */
    def allDefs (defs :Seq[DefElem]) :Seq[DefElem] = {
      def flatten (df :DefElem) :Seq[DefElem] = df +: df.defs.flatMap(flatten)
      defs flatMap(flatten)
    }

    def parse (elem :Elem) :CompUnitElem = {
      assert(elem.size == 1 && elem.head.label == "compunit",
             "DOM must be rooted in a single <compunit> element")
      val src = (elem \ "@src").text
      assert(src.length > 0, "<compunit> missing 'src' attribute.")
      CompUnitElem(src, collectValid(parse0(elem.head.child)))
    }

    // TODO: clean up this ugly hack
    protected def parse0 (elem :NodeSeq) :Seq[AnyRef] = {
      elem map(e => e.label match {
        case "def" => mkDef(e, parse0(e.child))
        case "use" => UseElem(
          (e \ "@name").text, (e \ "@target").text, parseKind(e), intAttr(e, "start"))
        case "sigdef" => SigDefElem(
          (e \ "@id").text, (e \ "@name").text, parseKind(e), intAttr(e, "start"))
        case x => null // will be #PCDATA or <sig> or <doc> which are handled elsewhere
      })
    }

    protected def mkDef (elem :Node, children :Seq[AnyRef]) :DefElem =
      DefElem((elem \ "@name").text, (elem \ "@id").text,
              (elem \ "sig").headOption map(parseSig), // we have zero or one <sig> blocks
              (elem \ "doc").headOption map(parseDoc), // we have zero or one <doc blocks
              parseKind(elem), parseFlavor(elem), parseFlags(elem), parseSupers(elem),
              intAttr(elem, "start"), intAttr(elem, "bodyStart"), intAttr(elem, "bodyEnd"),
              collectValid(children), collectValid(children))

    protected def parseKind (elem :Node) = {
      val text = (elem \ "@kind").text
      try {
        Enum.valueOf(classOf[Kind], text.toUpperCase)
      } catch {
        case e => println(elem + " -> " + e); Kind.UNKNOWN
      }
    }

    protected def parseFlavor (elem :Node) = {
      val text = (elem \ "@flavor").text
      try {
        Enum.valueOf(classOf[Flavor], text.toUpperCase)
      } catch {
        case e => e.printStackTrace; println(elem + " -> " + e); Flavor.NONE
      }
    }

    protected def parseFlags (elem :Node) = {
      val access = (elem \ "@access").text
      val flavor = parseFlavor(elem)
      // TODO: should we handle this in a less Java-centric way?
      var flags = 0
      if (access.equalsIgnoreCase("public")) flags |= JDef.PUBLIC
      if (!access.equalsIgnoreCase("private") &&
          flavor != Flavor.CONSTRUCTOR &&
          flavor != Flavor.TYPE_PARAM)       flags |= JDef.INHERITED
      flags
    }

    protected def parseSupers (elem :Node) = (elem \ "@supers").text.trim match {
      case "" => List()
      case ids => ids split(";") map(_.trim) toList
    }

    protected def parseSig (elem :Node) = {
      val children = parse0(elem.child)
      SigElem(elem.text.trim,
              children collect { case e :SigDefElem => e }, collectValid(children))
    }

    protected def parseDoc (elem :Node) = {
      val children = parse0(elem.child)
      DocElem(elem.text.trim, collectValid(children))
    }

    protected def filterValid[T <: Validatable] (elems :Seq[T]) :Seq[T] = {
      val (valid, invalid) = elems.partition(_.isValid)
      if (!invalid.isEmpty) {
        _log.warning("Dropping invalid elements " + invalid)
      }
      valid
    }

    protected def collectValid[T <: Validatable] (elems :Seq[AnyRef])(
      implicit m :Manifest[T]) :Seq[T] = {
      filterValid(elems collect { case e if (m.erasure.isInstance(e)) => e.asInstanceOf[T] })
    }

    protected def intAttr (elem :Node, attr :String) = {
      val text = (elem \ ("@" + attr)).text.trim
      try if (text.length == 0) 0 else text.toInt
      catch {
        case e :NumberFormatException => println(
          "Bogus element? " + attr + " -> " + text + " in " + elem); 0
      }
    }
  }
}
