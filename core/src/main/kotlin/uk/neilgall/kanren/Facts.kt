package uk.neilgall.kanren

private fun unify(facts: List<List<Term>>, props: List<Any?>): Goal {
  val propTerms = props.map(::term)
  return conj_(facts.map { conj_(it.zip(propTerms).map { it.first _is_ it.second }) })
}

fun relation2(vararg data: Any?): (Any?, Any?) -> Goal {
  assert(data.size % 2 == 0)
  val facts = data.map(::term).toList().chunked(2)
  return { a, b -> unify(facts, listOf(a, b)) }
}

fun relation3(vararg data: Any?): (Term, Term, Term) -> Goal {
  assert(data.size % 3 == 0)
  val facts = data.map(::term).toList().chunked(3)
  return { a, b, c -> unify(facts, listOf(a, b, c)) }
}

fun relation4(vararg data: Any?): (Term, Term, Term, Term) -> Goal {
  assert(data.size % 4 == 0)
  val facts = data.map(::term).toList().chunked(4)
  return { a, b, c, d -> unify(facts, listOf(a, b, c, d)) }
}

fun relation5(vararg data: Any?): (Term, Term, Term, Term, Term) -> Goal {
  assert(data.size % 5 == 0)
  val facts = data.map(::term).toList().chunked(5)
  return { a, b, c, d, e -> unify(facts, listOf(a, b, c, d, e)) }
}