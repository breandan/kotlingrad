package uk.neilgall.kanren

import uk.neilgall.kanren.BinaryOperation.*

@Suppress("RemoveRedundantQualifierName")
sealed class Term {
  object None: Term()
  data class String(val s: kotlin.String): Term()
  data class Int(val i: kotlin.Int): Term()
  data class Boolean(val b: kotlin.Boolean): Term()
  data class Variable(val v: kotlin.Int): Term()
  data class Pair(val p: Term, val q: Term): Term()
  data class BinaryExpr(val lhs: Term, val op: BinaryOperation, val rhs: Term): Term()

  override fun toString(): kotlin.String = when (this) {
    is Term.None -> "nil"
    is Term.String -> "\"$s\""
    is Term.Int -> i.toString()
    is Term.Boolean -> b.toString()
    is Term.Variable -> ".$v"
    is Term.Pair -> "($p, $q)"
    is Term.BinaryExpr -> "($lhs $op $rhs)"
  }
}

enum class BinaryOperation(val str: String) {
  PLUS("+"),
  MINUS("-"),
  MULTIPLY("*"),
  DIVIDE("/"),
  MOD("%"),
  AND("&&"),
  OR("||");

  override fun toString(): String = str
}

// Term construction
fun term(t: Any?): Term = if (t == null) Term.None else when (t) {
  is Term -> t
  is Int -> Term.Int(t)
  is String -> Term.String(t)
  is Boolean -> Term.Boolean(t)
  is Pair<*, *> -> Term.Pair(term(t.first), term(t.second))
  is List<*> -> t.map(::term).foldRight(Term.None, Term::Pair)
  else -> throw IllegalArgumentException()
}

// Lists using vararg
fun term(vararg t: Any?): Term = t.map(::term).foldRight(Term.None, Term::Pair)

// General operations
operator fun Term.plus(rhs: Term): Term = Term.BinaryExpr(this, PLUS, rhs)

operator fun Term.minus(rhs: Term): Term = Term.BinaryExpr(this, MINUS, rhs)
operator fun Term.times(rhs: Term): Term = Term.BinaryExpr(this, MULTIPLY, rhs)
operator fun Term.div(rhs: Term): Term = Term.BinaryExpr(this, DIVIDE, rhs)
operator fun Term.rem(rhs: Term): Term = Term.BinaryExpr(this, MOD, rhs)

// String operations
operator fun Term.plus(rhs: String): Term = this + term(rhs)

operator fun String.plus(rhs: Term): Term = term(this) + rhs

// Integer arithmetic
operator fun Term.plus(rhs: Int): Term = this + term(rhs)

operator fun Term.minus(rhs: Int): Term = this - term(rhs)
operator fun Term.times(rhs: Int): Term = this * term(rhs)
operator fun Term.div(rhs: Int): Term = this / term(rhs)
operator fun Term.rem(rhs: Int): Term = this % term(rhs)
operator fun Int.plus(rhs: Term): Term = term(this) + rhs
operator fun Int.minus(rhs: Term): Term = term(this) - rhs
operator fun Int.times(rhs: Term): Term = term(this) * rhs
operator fun Int.div(rhs: Term): Term = term(this) / rhs
operator fun Int.rem(rhs: Term): Term = term(this) % rhs

// Boolean operations
infix fun Term._and_(rhs: Term): Term = Term.BinaryExpr(this, AND, rhs)

infix fun Term._or_(rhs: Term): Term = Term.BinaryExpr(this, OR, rhs)
infix fun Term._and_(rhs: Boolean): Term = this _and_ term(rhs)
infix fun Term._or_(rhs: Boolean): Term = this _or_ term(rhs)
infix fun Boolean._and_(rhs: Term): Term = term(this) _and_ rhs
infix fun Boolean._or_(rhs: Term): Term = term(this) _or_ rhs

fun Term.toMatch(): Any? = when (this) {
  is Term.String -> s
  is Term.Int -> i
  is Term.Boolean -> b
  is Term.Pair -> {
    val pm = p.toMatch()
    val qm = q.toMatch()
    if (qm == null) listOf(pm) else when (qm) {
      is List<*> -> listOf(pm) + qm
      else -> Pair(pm, qm)
    }
  }
  else -> null
}