package uk.neilgall.kanren

import io.kotlintest.specs.StringSpec

class ComplexExample: StringSpec({
  "DOG + CAT = BAD" {
    run { d, o, g, c, a, t, b ->
      fresh { d1, d2, d3, d4, d5, d6, d7, d8 ->
        conj(
          d1 _is_ term(0, 1, 2, 3, 4, 5, 6),
          removeo(d, d2, d1),
          removeo(o, d3, d2),
          removeo(g, d4, d3),
          removeo(c, d5, d4),
          removeo(a, d6, d5),
          removeo(t, d7, d6),
          removeo(b, d8, d7),
          d + c _is_ b,
          o + a _is_ a,
          g + t _is_ d
        )
      }
    }.forEach { println(it) }
  }
})