/*
 * rectangle-intellij-plugin
 *
 * Copyright 2024 - Brice Dutheil
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package io.github.bric3.rectangle.util

import kotlinx.coroutines.delay
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.pow
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Executes [block] immediately and upon failure retries the given [block] at most [maxRetries] if [retryIf] returns `true`.
 *
 * Before each retry, the coroutine is delayed using exponential backoff with full jitter.
 * The delay is computed as a random value between 0 and the exponentially increasing maximum delay.
 *
 * Also, note there's no retry on coroutine cancellation.
 *
 * [Reference article on exponential backoff with full jitter](https://aws.amazon.com/blogs/architecture/exponential-backoff-and-jitter/).
 *
 * @param maxRetries the maximum number of retries
 * @param initialDelay the initial delay between retries
 * @param maxDelay the maximum delay between retries
 * @param factor the factor by which the delay increases
 * @param retryIf a predicate that determines if the block should be retried
 * @param block the block to retry
 */
suspend fun <T> retry(
  maxRetries: Int = 3,
  initialDelay: Duration = 100.milliseconds,
  maxDelay: Duration = 10.seconds,
  factor: Double = 2.0,
  retryIf: (Throwable) -> Boolean = { true },
  block: suspend () -> T
): T {
  require(maxRetries > 1) { "maxRetries must be greater than 1" }
  repeat(maxRetries - 1) { attempt ->
    try {
      return block()
    } catch (ce: CancellationException) {
      throw ce
    } catch (e: Throwable) {
      // you can log an error here and/or make a more finer-grained
      // analysis of the cause to see if retry is needed
      if (attempt < maxRetries - 1 && retryIf(e)) {
        val expDelay = (initialDelay.inWholeMilliseconds * factor.pow(attempt)).toLong()
        val jitteredDelay = Random.nextLong(0, expDelay.coerceAtMost(maxDelay.inWholeMilliseconds))
        delay(jitteredDelay.milliseconds)
      } else {
        throw e
      }
    }
  }
  return block() // last attempt
}
