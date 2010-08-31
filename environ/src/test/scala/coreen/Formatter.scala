//
// $Id$

package coreen

import _root_.java.io.File

import scala.io.Source

import coreen.java.Reader

/**
 * A simple test program that parses a Java file and renders it to HTML with defs and uses
 * highlighted.
 */
object Formatter
{
  def main (args :Array[String]) {
    for (file <- args) {
      val xml = Reader.process(List(new File(file))).head
      val tree = Model.parse(xml)

      val source = Source.fromFile(file).getLines.mkString("\n")
      println("<pre>")
      println(source.substring(0, tree.start) + "<b>" + source.substring(tree.start, tree.end) + "</b>" + source.substring(tree.end))
      println("</pre>")
    }
  }
}
