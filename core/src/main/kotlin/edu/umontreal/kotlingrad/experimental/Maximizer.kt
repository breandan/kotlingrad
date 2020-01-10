package edu.umontreal.kotlingrad.experimental

tailrec fun <I, O: Comparable<O>> minimize(fn: (I) -> (O), min: I, budget: Int, rand: () -> I): I =
  if (budget <= 0) min
  else minimize(fn, rand().let { input ->
    if (fn(input) < fn(min)) input else min
  }, budget - 1, rand)


interface Metric<T: Metric<T>>: Comparable<T> {
  operator fun plus(metric: T): T
  operator fun minus(metric: T): T
}

tailrec fun <I, O: Metric<O>> minimizeMetric(
  fn: (I) -> (O), min: I, budget: Int): I =
  if (budget <= 0) min
  else minimizeMetric(fn, wiggle(min).filter { fn(it) < fn(min) }
    .maxBy { fn(min) - fn(it) } ?: min, budget - 1)

fun <I> wiggle(min: I): Sequence<I> = TODO()

tailrec fun <T: Field<T>> minimizeField(
  fn: (T) -> (T), a: T, least: T, budget: Int): T =
  if (budget <= 0) least
  else minimizeField(fn, a,
    least - (fn(least) - fn(least + a)) / a,
    budget - 1)

val t: (Int, Int) -> Pair<Int, Int>? = { i, x -> null }

class Input
class Output

fun unitTest(subroutine: (Input) -> Output) {
  val input = Input() // Construct an input
  val expectedOutput = Output() // Construct an output
  val actualOutput = subroutine(input)
  assert(expectedOutput == actualOutput) { "Expected $expectedOutput, got $actualOutput" }
}

fun <I, O> integrationTest(program: (I) -> O, inputs: Set<I>, checkOutput: (O) -> Boolean) =
  inputs.forEach { input ->
    try {
      val output = program(input)
      assert(checkOutput(output)) { "Postcondition failed on $input, $output" }
    } catch (exception: Exception) {
      assert(false) { exception }
    }
  }

fun <I, O> fuzzTest(program: (I) -> O, oracle: (I) -> O, rand: () -> I) =
  repeat(1000) {
    val input = rand()
    assert(program(input) == oracle(input)) { "Oracle and program disagree on $input" }
  }

fun <I, O> gen(program: (I) -> O, property: (O) -> Boolean, rand: () -> I) =
  repeat(1000) {
    val randomInput = rand()

    assert(property(program(randomInput))) {
      val shrunken = shrink(randomInput, program, property)
      "Minimal input counterexample of property: $shrunken"
    }
  }

tailrec fun <I, O> shrink(failure: I, program: (I) -> O, property: (O) -> Boolean): I =
  if (property(program(decrease(failure)))) failure // Property holds once again
  else shrink(decrease(failure), program, property) // Decrease until property holds

fun <I> decrease(failure: I): I = TODO()

fun decrease(failure: Float) = failure - failure / 2

fun <I, O> mrTest(program: (I) -> O, mr: (I, O, I, O) -> Boolean, rand: () -> Pair<I, O>) =
  repeat(1000) {
    val (input: I, output: O) = rand()
    val tx: (I) -> I = genTX(program, mr, input, output)
    val txInput: I = tx(input)
    val txOutput: O = program(txInput)
    assert(mr(input, output, txInput, txOutput)) {
      "<$input, $output> not related to <$txInput, $txOutput> by $mr ($tx)"
    }
  }

fun <I, O> genTX(program: (I) -> O, mr: (I, O, I, O) -> Boolean, i: I, o: O): (I) -> I {
  while (true) {
    val tx: (I) -> I = genRandomTx() // Samples a random transformation
    val txInput: I = tx(i)
    val txOutput: O = program(txInput)
    if (mr(i, o, txInput, txOutput)) return tx
  }
}

fun <I> genRandomTx(): (I) -> I = TODO()