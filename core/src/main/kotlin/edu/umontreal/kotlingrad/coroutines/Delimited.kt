package edu.umontreal.kotlingrad.coroutines

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

/*
 * Taken from: https://gist.github.com/elizarov/5bbbe5a3b88985ae577d8ec3706e85ef
 *
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
  DelimitedScopeImpl<T>().also { impl ->
    body.startCoroutine(impl, impl)
  }.runReset()

interface DelimitedContinuation<T, R>

@RestrictsSuspension
abstract class DelimitedScope<T> {
  abstract suspend fun <R> shift(block: suspend DelimitedScope<T>.(DelimitedContinuation<T, R>) -> T): R
  abstract suspend operator fun <R> DelimitedContinuation<T, R>.invoke(value: R): T
}

private typealias ShiftedFun<T> = (DelimitedScope<T>, DelimitedContinuation<T, Any?>, Continuation<T>) -> Any?

@Suppress("UNCHECKED_CAST")
private class DelimitedScopeImpl<T>: DelimitedScope<T>(), Continuation<T>,
  DelimitedContinuation<T, Any?> {
  private var shifted: ShiftedFun<T>? = null
  private var shiftCont: Continuation<Any?>? = null
  private var invokeCont: Continuation<T>? = null
  private var invokeValue: Any? = null
  private var result: Result<T>? = null

  override val context: CoroutineContext
    get() = EmptyCoroutineContext

  override fun resumeWith(result: Result<T>) {
    this.result = result
  }

  override suspend fun <R> shift(block: suspend DelimitedScope<T>.(DelimitedContinuation<T, R>) -> T): R =
    suspendCoroutineUninterceptedOrReturn {
      this.shifted = block as ShiftedFun<T>
      this.shiftCont = it as Continuation<Any?>
      COROUTINE_SUSPENDED
    }

  override suspend fun <R> DelimitedContinuation<T, R>.invoke(value: R): T =
    suspendCoroutineUninterceptedOrReturn sc@{
      check(invokeCont == null)
      invokeCont = it
      invokeValue = value
      COROUTINE_SUSPENDED
    }

  fun runReset(): T {
    // This is the stack of continuation in the `shift { ... }` after call to delimited continuation
    var currentCont: Continuation<T> = this
    // Trampoline loop to avoid call stack usage
    loop@ while (true) {
      // Call shift { ... } body or break if there are no more shift calls
      val shifted = takeShifted() ?: break
      // If shift does not call any continuation, then its value becomes the result -- break out of the loop
      try {
        val value = shifted.invoke(this, this, currentCont)
        if (value !== COROUTINE_SUSPENDED) {
          result = Result.success(value as T)
          break
        }
      } catch (e: Throwable) {
        result = Result.failure(e)
        break
      }
      // Shift has suspended - check if shift { ... } body had invoked continuation
      currentCont = takeInvokeCont() ?: continue@loop
      val shiftCont = takeShiftCont()
        ?: error("Delimited continuation is single-shot and cannot be invoked twice")
      shiftCont.resume(invokeValue)
    }
    // Propagate the result to all pending continuations in shift { ... } bodies
    currentCont.resumeWith(result!!)
    // Return the final result
    return result!!.getOrThrow()
  }

  private fun takeShifted() = shifted?.also { shifted = null }
  private fun takeShiftCont() = shiftCont?.also { shiftCont = null }
  private fun takeInvokeCont() = invokeCont?.also { invokeCont = null }
}