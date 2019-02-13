package edu.umontreal.kotlingrad.coroutines

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

/**
 * Implementation for Delimited Continuations `shift`/`reset` primitives via Kotlin Coroutines.
 * See [https://en.wikipedia.org/wiki/Delimited_continuation].
 *
 * The following LISP code:
 *
 * ```
 * (* 2 (reset (+ 1 (shift k (k 5)))))
 * ```
 *
 * Translates to:
 *
 * ```
 * 2 * reset<Int> { 1 + shift<Int> { k -> k(5) } }
 * ```
 */

fun <T> reset(body: suspend DelimitedScope<T>.() -> T): T =
    DelimitedScopeImpl<T>().also { body.startCoroutine(it, it) }.runReset()

interface DelimitedContinuation<T, R> {
  fun invokeWith(value: Result<R>): T
}

operator fun <T, R> DelimitedContinuation<T, R>.invoke(value: R): T = invokeWith(Result.success(value))

@RestrictsSuspension
abstract class DelimitedScope<T> { abstract suspend fun <R> shift(block: (DelimitedContinuation<T, R>) -> T): R }

private class DelimitedScopeImpl<T> : DelimitedScope<T>(), Continuation<T>, DelimitedContinuation<T, Any?> {
  private var shifted: ((DelimitedContinuation<T, Any?>) -> T)? = null
  private var cont: Continuation<Any?>? = null
  private var result: Result<T>? = null

  override val context: CoroutineContext
    get() = EmptyCoroutineContext

  override fun resumeWith(result: Result<T>) {
    this.result = result
  }

  @Suppress("UNCHECKED_CAST")
  override suspend fun <R> shift(block: (DelimitedContinuation<T, R>) -> T): R =
      suspendCoroutineUninterceptedOrReturn {
        shifted = block as ((DelimitedContinuation<T, Any?>) -> T)
        cont = it as Continuation<Any?>
        COROUTINE_SUSPENDED
      }

  override fun invokeWith(value: Result<Any?>): T {
    val cont = this.cont?.also { cont = null } ?: error("Delimited continuation is single-shot and cannot be invoked twice")
    cont.resumeWith(value)
    return takeResult()
  }

  tailrec fun runReset(): T {
    val shifted = shifted?.also { shifted = null } ?: return takeResult()
    result = runCatching { shifted(this) }
    return runReset()
  }

  private fun takeResult(): T {
    val result = this.result
    check(result != null)
    this.result = null
    return result.getOrThrow()
  }
}