package ai.hypergraph.kotlingrad.typelevel

import java.math.BigDecimal as BD
import java.math.BigInteger as BI
import org.junit.jupiter.api.Test
import java.math.*
import kotlin.system.measureTimeMillis

class TypeClassTest {
  val max = 10L

  @Test
  fun benchmark() =
    listOf(
      BaseType(
        max = BD.valueOf(max),
        one = BD.ONE,
        nil = BD.ZERO,
        plus = { a, b -> a + b },
        minus = { a, b -> a - b },
        times = { a, b -> a * b },
        div = { a, b -> a / b },
      ),
      BaseType(
        max = BI.valueOf(max),
        one = BI.ONE,
        nil = BI.ZERO,
        plus = { a, b -> a + b },
        minus = { a, b -> a - b },
        times = { a, b -> a * b },
        div = { a, b -> a / b },
      ),
      BaseType(
        max = max, one = 1L, nil = 0L,
        plus = { a, b -> a + b }, minus = { a, b -> a - b },
        times = { a, b -> a * b }, div = { a, b -> a / b },
      ),
      BaseType(
        max = max.toFloat(), one = 1f, nil = 0f,
        plus = { a, b -> a + b }, minus = { a, b -> a - b },
        times = { a, b -> a * b }, div = { a, b -> a / b },
      ),
      BaseType(
        max = Rational(max.toInt()),
        nil = Rational.ZERO, one = Rational.ONE,
        plus = { a, b -> a + b }, times = { a, b -> a * b },
        div = { a, b -> a / b }, minus = { a, b -> a - b }
      )
    ) // Benchmark all (types x algebras)
      .forEach { baseType ->
        baseType.algebras().forEach {
          it.benchmark(baseType.max)
        }
      }

  @Test
  fun vectorFieldTest() =
    VectorField(f = Field(
      nil = BI.ZERO,
      one = BI.ONE,
      plus = { a, b -> a + b },
      times = { a, b -> a * b },
      div = { a, b -> a / b },
      minus = { a, b -> a - b }
    )).run {
      println(BI.ONE dot Vector(BI.ZERO, BI.ONE))
      println(Vector(BI.ZERO, BI.ONE) + Vector(BI.ONE, BI.ONE))
    }

  fun <T> BaseType<T>.algebras(): List<Nat<T>> = listOf(
    Nat(
      nil = nil,
      next = { this + one }
    ),
    Group(
      nil = nil, one = one,
      plus = { a, b -> a + b }
    ),
    Ring(
      nil = nil, one = one,
      plus = { a, b -> a + b },
      times = { a, b -> a * b }
    ),
    Field(
      nil = nil, one = one,
      plus = { a, b -> a + b },
      times = { a, b -> a * b },
      div = { a, b -> a / b },
      minus = { a, b -> a - b }
    )
  )

  data class BaseType<T>(
    val max: T, val one: T, val nil: T,
    val plus: (T, T) -> T,
    val times: (T, T) -> T,
    val minus: (T, T) -> T,
    val div: (T, T) -> T,
  ) {
    operator fun T.plus(that: T) = plus(this, that)
    operator fun T.times(that: T) = times(this, that)
    operator fun T.minus(that: T) = minus(this, that)
    operator fun T.div(that: T) = div(this, that)
  }

  fun <T> Nat<T>.benchmark(max: Any) =
    measureTimeMillis {
      println(
        javaClass.interfaces.first().simpleName + "<${nil!!::class.java.simpleName}>" + " results\n" +
          "\tFibonacci: " + fibonacci(max as T) + "\n" +
          "\tPrimes:    " + primes(max as T) + "\n" +
          "\tPower:     " + (one + one).pow(max as T) + "\n" +
          "\tFactorial: " + factorial(max as T)
      )
    }.also { ms -> println("Total: ${ms}ms\n") }
}