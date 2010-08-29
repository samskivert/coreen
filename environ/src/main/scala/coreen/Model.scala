//
// $Id$

package coreen

import model.{Def => JDef, Use => JUse}

/**
 * Models a source file as a nested collection of definitions and uses.
 */
class Model
{
  /** Tracks a span of characters in a source file. */
  trait Span {
    val start :Int
    val length :Int
  }

  /** Models a definition (e.g. class, field, function, method, variable). */
  case class Def (id :Int, parent :Def, name :String, typ :JDef.Type, defs :Seq[Def],
                  bodyStart :Int, uses :Seq[Use], start :Int, length :Int) extends Span {
    /** Converts this Scala model class into its Java equivalent (for interop with GWT). */
    def toJava :JDef = new JDef(id, parent.toJava, name, typ, defs map(_.toJava) toArray,
                                bodyStart, uses map(_.toJava) toArray, start, length)
  }

  /** Models the use of a name somewhere in a source file. */
  case class Use (id :Int, owner :Def, referent :Def, start :Int, length :Int) extends Span {
    /** Converts this Scala model class into its Java equivalent (for interop with GWT). */
    def toJava :JUse = new JUse(id, owner.toJava, referent.toJava, start, length)
  }
}
