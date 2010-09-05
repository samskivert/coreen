//
// $Id$

package coreen

import _root_.java.io.File

import scala.io.Source

import coreen.java.Reader
import coreen.nml.SourceModel

/**
 * A simple test program that parses a Java file and renders it to HTML with defs and uses
 * highlighted.
 */
object Formatter
{
  def main (args :Array[String]) {
    for (file <- args) {
      val xml = Reader.process(List(new File(file))).head
      val tree = SourceModel.parse(xml)

      val source = Source.fromFile(file).getLines.mkString("\n")
      val edits = tree.toEdits(source).sortBy(_.start).toList

      println("<pre>")
      println(applyEdits(source, 0, edits))
      println("</pre>")
    }
  }

  def applyEdits (source :String, last :Int, edits :List[SourceModel.Edit]) :String = edits match {
    case Nil => source.substring(last)
    case h :: t => (source.substring(last, h.start) +
                    "<u>" + source.substring(h.start, h.end) + "</u>" +
                    applyEdits(source, h.end, t))
  }
}
