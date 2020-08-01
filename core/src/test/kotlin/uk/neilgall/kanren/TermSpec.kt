package uk.neilgall.kanren

import io.kotlintest.properties.forAll
import io.kotlintest.specs.StringSpec

class TermSpec: StringSpec({

    "Integers can be terms" {
        forAll { n: Int -> term(n) == Term.Int(n) }
    }

    "Strings can be terms" {
        forAll { s: String -> term(s) == Term.String(s) }
    }

    "Booleans can be terms" {
        forAll { b: Boolean -> term(b) == Term.Boolean(b) }
    }

    "Pairs can be terms" {
        forAll { a: Int, b: String -> term(Pair(a, b)) == Term.Pair(term(a), term(b)) }
    }

    "Lists can be terms" {
        forAll { xs: List<Int> -> term(xs) == xs.map { term(it) }.foldRight(Term.None, Term::Pair) }
    }
})