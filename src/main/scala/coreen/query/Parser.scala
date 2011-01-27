//
// $Id$

package coreen.query

import scala.util.parsing.combinator._

/**
 * A combinator parser for the Coreen query language.
 */
object Parser extends JavaTokenParsers with PackratParsers
{
  /** Parses the supplied input, returning a query AST. */
  def parse (input :String) = parseAll(expr, input)

  // val Ident = """[a-zA-Z]([a-zA-Z0-9]|_[a-zA-Z0-9])*"""r // TODO: broaden

  private lazy val expr :PackratParser[Any] =
    ( logic | `type` | kind | path | use | heir | data )

  private lazy val term :PackratParser[Any] = "(" ~ expr ~ ")" | ident

  // kind ::= M expr | T expr | F expr | V expr
  // use ::= @expr | expr() | ?=expr | expr=?
  // heir ::= +expr | -expr | ++expr | --expr
  // data ::= >>expr | <<expr | >>>expr | <<<expr
  // type ::= expr:expr
  // path ::= expr.expr | expr..expr
  // logic ::= expr && expr | expr || expr

  private lazy val path   :PackratParser[Any] = term ~ rep(("." | "..") ~ term)

  private lazy val kind   :PackratParser[Any] = "M"~path | "T"~path | "F"~path | "V"~path
  private lazy val use    :PackratParser[Any] = "@"~path | path~"()" | "?="~path | path~"=?"
  private lazy val heir   :PackratParser[Any] = "+"~path | "-"~path | "++"~path | "--"~path
  private lazy val data   :PackratParser[Any] = ">>"~path | "<<"~path | ">>>"~path | "<<<"~path
  private lazy val `type` :PackratParser[Any] = path ~ ":" ~ path
  private lazy val logic  :PackratParser[Any] = path ~ rep(("&&" | "||") ~ path)
}
