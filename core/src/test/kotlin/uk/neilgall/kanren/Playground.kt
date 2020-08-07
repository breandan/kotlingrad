package uk.neilgall.kanren

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

class PlaygroundTests: StringSpec({
  "can debug" {
    run { a -> trace("a is 3")(a _is_ 3) }.first() shouldBe listOf(3)
  }

  "can evaluate simple arithmetic" {
    run { a -> a + 5 _is_ 9 }.first() shouldBe listOf(4)
  }

  "can do indeterminate boolean logic" {
    run { a -> a _and_ false _is_ false } shouldBe listOf(listOf(false), listOf(true))
  }

  "can generate infinite streams" {
    fun fives(t: Term): Goal = t _is_ 5 _or_ fresh(::fives)
    fun sixes(t: Term): Goal = t _is_ 6 _or_ fresh(::sixes)

    runGoal(10, fresh(::fives)) shouldBe listOf(listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5), listOf(5))

    // TODO: won't work until search strategy is fixed
    // runGoal(10, fresh(::fives) _or_ fresh(::sixes)) shouldBe listOf(listOf(5), listOf(6), listOf(5), listOf(6), listOf(5), listOf(6), listOf(5), listOf(6), listOf(5), listOf(6))
  }

  "can unify lists" {
    run { a -> a _is_ listOf(1, 2, 3) }.first() shouldBe listOf(listOf(1, 2, 3))
  }

  "can perform list appends" {
    run { a -> appendo(a, term(4, 5), term(1, 2, 3, 4, 5)) }.first() shouldBe listOf(listOf(1, 2, 3))
  }

  "can perform indeterminate list appends" {
    run { a, b -> appendo(a, b, term(1, 2, 3, 4, 5)) } shouldBe listOf(
      listOf(null, listOf(1, 2, 3, 4, 5)),
      listOf(listOf(1), listOf(2, 3, 4, 5)),
      listOf(listOf(1, 2), listOf(3, 4, 5)),
      listOf(listOf(1, 2, 3), listOf(4, 5)),
      listOf(listOf(1, 2, 3, 4), listOf(5)),
      listOf(listOf(1, 2, 3, 4, 5), null)
    )
  }

  "can find list members" {
    run { a -> membero(a, term(1, 2, 3, 4, 5)) } shouldBe listOf(listOf(1), listOf(2), listOf(3), listOf(4), listOf(5))
  }

  "can remove list members" {
    run { a, b -> removeo(a, b, term(1, 2, 3, 4, 5)) } shouldBe listOf(
      listOf(1, listOf(2, 3, 4, 5)),
      listOf(2, listOf(1, 3, 4, 5)),
      listOf(3, listOf(1, 2, 4, 5)),
      listOf(4, listOf(1, 2, 3, 5)),
      listOf(5, listOf(1, 2, 3, 4))
    )
  }

  "can deal with relations" {
    val parent = relation2(
      "Homer", "Bart",
      "Homer", "Lisa",
      "Homer", "Maggie",
      "Marge", "Bart",
      "Marge", "Lisa",
      "Marge", "Maggie",
      "Abe", "Homer"
    )

    fun grandparent(a: Term, b: Term): Goal = fresh { c -> parent(a, c) _and_ parent(c, b) }

    // TODO: fix broken test
    //  run { a -> parent(a, "Bart") } shouldBe listOf(listOf("Homer"), listOf("Marge"))
    //  run { a -> parent("Homer", a) } shouldBe listOf(listOf("Bart"), listOf("Lisa"), listOf("Maggie"))
    //  run { a -> grandparent(a, term("Bart")) } shouldBe listOf(listOf("Abe"))
  }
})