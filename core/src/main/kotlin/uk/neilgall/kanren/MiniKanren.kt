package uk.neilgall.kanren

fun condo(vararg clauses: List<Goal>): Goal = disj_(clauses.map(::conj_))

fun appendo(xs: Term, ys: Term, zs: Term): Goal =
  ((xs _is_ Term.None) _and_ (ys _is_ zs)) _or_
    fresh { xhead, xtail, ztail ->
      conj(
        xs _is_ Term.Pair(xhead, xtail),
        zs _is_ Term.Pair(xhead, ztail),
        appendo(xtail, ys, ztail)
      )
    }

fun membero(x: Term, ys: Term): Goal =
  fresh { head, tail -> Term.Pair(head, tail) _is_ ys _and_ (x _is_ head _or_ membero(x, tail)) }

fun lengtho(length: Term, xs: Term): Goal =
  conj(xs _is_ Term.None, length _is_ Term.Int(0)) _or_
    fresh { head, tail, tailLength ->
      conj(
        Term.Pair(head, tail) _is_ xs,
        lengtho(tailLength, tail),
        length _is_ tailLength + 1
      )
    }

fun removeo(x: Term, xs: Term, ys: Term): Goal =
  fresh { p, q, r ->
    conj(
      q _is_ Term.Pair(x, r),
      appendo(p, q, ys),
      appendo(p, r, xs)
    )
  }