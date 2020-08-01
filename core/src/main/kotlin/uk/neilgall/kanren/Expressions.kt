package uk.neilgall.kanren

fun BinaryOperation.evaluate(lhs: Term, rhs: Term): Term? = when (this) {

  BinaryOperation.PLUS -> when {
    lhs is Term.Int && rhs is Term.Int -> Term.Int(lhs.i + rhs.i)
    lhs is Term.String && rhs is Term.String -> Term.String(lhs.s + rhs.s)
    else -> null
  }

  BinaryOperation.MINUS -> when {
    lhs is Term.Int && rhs is Term.Int -> Term.Int(lhs.i - rhs.i)
    else -> null
  }

  BinaryOperation.MULTIPLY -> when {
    lhs is Term.Int && rhs is Term.Int -> Term.Int(lhs.i * rhs.i)
    else -> null
  }

  BinaryOperation.DIVIDE -> when {
    lhs is Term.Int && rhs is Term.Int -> Term.Int(lhs.i / rhs.i)
    else -> null
  }

  BinaryOperation.MOD -> when {
    lhs is Term.Int && rhs is Term.Int -> Term.Int(lhs.i % rhs.i)
    else -> null
  }

  BinaryOperation.AND -> when {
    lhs is Term.Boolean && rhs is Term.Boolean -> Term.Boolean(lhs.b && rhs.b)
    else -> null
  }

  BinaryOperation.OR -> when {
    lhs is Term.Boolean && rhs is Term.Boolean -> Term.Boolean(lhs.b || rhs.b)
    else -> null
  }
}

internal val ALL_BOOLS = sequenceOf(Term.Boolean(false), Term.Boolean(true))

fun BinaryOperation.reverseEvaluateLHS(rhs: Term, result: Term): Sequence<Term>? = when (this) {

  BinaryOperation.PLUS -> when {
    result is Term.Int && rhs is Term.Int -> sequenceOf(Term.Int(result.i - rhs.i))
    else -> null
  }

  BinaryOperation.MINUS -> when {
    result is Term.Int && rhs is Term.Int -> sequenceOf(Term.Int(result.i + rhs.i))
    else -> null
  }

  BinaryOperation.MULTIPLY -> when {
    result is Term.Int && rhs is Term.Int -> sequenceOf(Term.Int(result.i / rhs.i))
    else -> null
  }

  BinaryOperation.DIVIDE -> when {
    result is Term.Int && rhs is Term.Int -> sequenceOf(Term.Int(result.i * rhs.i))
    else -> null
  }

  BinaryOperation.MOD -> null

  BinaryOperation.AND -> when {
    result is Term.Boolean && rhs is Term.Boolean -> if (rhs.b) sequenceOf(result) else ALL_BOOLS
    else -> null
  }

  BinaryOperation.OR -> when {
    result is Term.Boolean && rhs is Term.Boolean -> if (rhs.b) ALL_BOOLS else sequenceOf(result)
    else -> null
  }
}

fun BinaryOperation.reverseEvaluateRHS(lhs: Term, result: Term): Sequence<Term>? = when (this) {

  BinaryOperation.PLUS -> when {
    result is Term.Int && lhs is Term.Int -> sequenceOf(Term.Int(result.i - lhs.i))
    else -> null
  }

  BinaryOperation.MINUS -> when {
    result is Term.Int && lhs is Term.Int -> sequenceOf(Term.Int(-(result.i - lhs.i)))
    else -> null
  }

  BinaryOperation.MULTIPLY -> when {
    result is Term.Int && lhs is Term.Int -> sequenceOf(Term.Int(result.i / lhs.i))
    else -> null
  }

  BinaryOperation.DIVIDE -> when {
    result is Term.Int && lhs is Term.Int -> sequenceOf(Term.Int(result.i * lhs.i))
    else -> null
  }

  BinaryOperation.MOD -> null

  BinaryOperation.AND -> when {
    result is Term.Boolean && lhs is Term.Boolean -> if (lhs.b) sequenceOf(result) else ALL_BOOLS
    else -> null
  }

  BinaryOperation.OR -> when {
    result is Term.Boolean && lhs is Term.Boolean -> if (!lhs.b) sequenceOf(result) else ALL_BOOLS
    else -> null
  }
}