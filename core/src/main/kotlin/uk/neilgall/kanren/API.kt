package uk.neilgall.kanren

val emptyState = State()

infix fun Term._is_(rhs: Term): Goal = { state -> state.unify(this, rhs) }
infix fun Term._is_not_(rhs: Term): Goal = { state -> state.disunify(this, rhs) }

infix fun Term._is_(rhs: Int): Goal = this _is_ Term.Int(rhs)
infix fun Term._is_(rhs: String): Goal = this _is_ Term.String(rhs)
infix fun Term._is_(rhs: Boolean): Goal = this _is_ Term.Boolean(rhs)
infix fun <A, B> Term._is_(rhs: Pair<A, B>) = this _is_ term(rhs)
infix fun <T> Term._is_(rhs: List<T>) = this _is_ term(rhs)

fun conj_(gs: List<Goal>): Goal = when {
  gs.isEmpty() -> { _ -> sequenceOf() }
  gs.size == 1 -> gs.first()
  else -> { state -> gs.fold(sequenceOf(state), { states, goal -> states.flatMap(zzz(goal)) }) }
}

fun disj_(gs: List<Goal>): Goal = when {
  gs.isEmpty() -> { _ -> sequenceOf() }
  gs.size == 1 -> gs.first()
  else -> { state -> gs.fold(sequenceOf(), { states, goal -> states + zzz(goal)(state) }) }
}

fun conj(vararg gs: Goal): Goal = conj_(gs.asList())
fun disj(vararg gs: Goal): Goal = disj_(gs.asList())

infix fun Goal._and_(rhs: Goal) = conj_(listOf(this, rhs))
infix fun Goal._or_(rhs: Goal) = disj_(listOf(this, rhs))

fun fresh(f: (Term) -> Goal): Goal = { state -> state.withNewVar(f) }

// Convenience functions for introducing multiple variables at once
fun fresh(f: (Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> f(a, b) } }

fun fresh(f: (Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> f(a, b, c) } } }

fun fresh(f: (Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> f(a, b, c, d) } } } }

fun fresh(f: (Term, Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> fresh { e -> f(a, b, c, d, e) } } } } }

fun fresh(f: (Term, Term, Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> fresh { e -> fresh { f -> f(a, b, c, d, e, f) } } } } } }

fun fresh(f: (Term, Term, Term, Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> fresh { e -> fresh { f -> fresh { g -> f(a, b, c, d, e, f, g) } } } } } } }

fun fresh(f: (Term, Term, Term, Term, Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> fresh { e -> fresh { f -> fresh { g -> fresh { h -> f(a, b, c, d, e, f, g, h) } } } } } } } }

fun fresh(f: (Term, Term, Term, Term, Term, Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> fresh { e -> fresh { f -> fresh { g -> fresh { h -> fresh { i -> f(a, b, c, d, e, f, g, h, i) } } } } } } } } }

fun fresh(f: (Term, Term, Term, Term, Term, Term, Term, Term, Term, Term) -> Goal): Goal =
  fresh { a -> fresh { b -> fresh { c -> fresh { d -> fresh { e -> fresh { f -> fresh { g -> fresh { h -> fresh { i -> fresh { j -> f(a, b, c, d, e, f, g, h, i, j) } } } } } } } } } }

typealias Match = List<Any?>
typealias KanrenResult = List<Match>

fun reify(n: Int): (State) -> Match = { state ->
  0.until(n).map { state.walk(Term.Variable(it)).toMatch() }
}

fun reifyMatching(state: State): Match =
  0.until(state.vars).mapNotNull { state.walk(Term.Variable(it)).toMatch() }

fun take(n: Int? = null, goal: Goal): List<State> {
  val states = goal(emptyState)
  return if (n === null) states.toList() else states.take(n).toList()
}

fun runGoal(n: Int? = null, goal: Goal): KanrenResult =
  take(n, goal).map(::reifyMatching)

fun run(n: Int? = null, goal: (Term) -> Goal): KanrenResult =
  take(n, fresh { a -> goal(a) }).map(reify(1))

fun run(n: Int? = null, goal: (Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b -> goal(a, b) }).map(reify(2))

fun run(n: Int? = null, goal: (Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c -> goal(a, b, c) }).map(reify(3))

fun run(n: Int? = null, goal: (Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d -> goal(a, b, c, d) }).map(reify(4))

fun run(n: Int? = null, goal: (Term, Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d, e -> goal(a, b, c, d, e) }).map(reify(5))

fun run(n: Int? = null, goal: (Term, Term, Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d, e, f -> goal(a, b, c, d, e, f) }).map(reify(6))

fun run(n: Int? = null, goal: (Term, Term, Term, Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d, e, f, g -> goal(a, b, c, d, e, f, g) }).map(reify(7))

fun run(n: Int? = null, goal: (Term, Term, Term, Term, Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d, e, f, g, h -> goal(a, b, c, d, e, f, g, h) }).map(reify(8))

fun run(n: Int? = null, goal: (Term, Term, Term, Term, Term, Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d, e, f, g, h, i -> goal(a, b, c, d, e, f, g, h, i) }).map(reify(9))

fun run(n: Int? = null, goal: (Term, Term, Term, Term, Term, Term, Term, Term, Term, Term) -> Goal): KanrenResult =
  take(n, fresh { a, b, c, d, e, f, g, h, i, j -> goal(a, b, c, d, e, f, g, h, i, j) }).map(reify(10))