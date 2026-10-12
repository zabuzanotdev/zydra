package com.zydra.domain.engine

import com.zydra.domain.cracker.MatchResult
import com.zydra.domain.cracker.SinglePasswordTester
import com.zydra.domain.model.CrackFailure
import com.zydra.domain.model.CrackingProgress
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference

class ParallelCrackEngine(
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default,
    private val workerCount: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
) {

    fun crack(
        candidates: Sequence<String>,
        totalCount: Long,
        tester: SinglePasswordTester,
        progressIntervalMs: Long = PROGRESS_INTERVAL_MS
    ): Flow<CrackingProgress> = flow {
        if (totalCount <= 0) {
            emit(CrackingProgress.Failed(CrackFailure.NoCandidate))
            return@flow
        }

        val iterator = candidates.iterator()
        val iteratorLock = Any()
        val tried = AtomicLong(0)
        val stopFlag = AtomicBoolean(false)
        val foundPassword = AtomicReference<String?>(null)
        val foundDetail = AtomicReference<String?>(null)
        val startNanos = System.nanoTime()

        coroutineScope {
            repeat(workerCount) {
                launch(workerDispatcher) {
                    while (!stopFlag.get() && isActive) {
                        val batch = takeNextBatch(iterator, iteratorLock) ?: break
                        for (password in batch) {
                            if (stopFlag.get() || !isActive) break
                            val result = try {
                                tester.matches(password)
                            } catch (_: Exception) {
                                MatchResult.NoMatch
                            }
                            if (result is MatchResult.Matched) {
                                if (stopFlag.compareAndSet(false, true)) {
                                    foundPassword.set(password)
                                    foundDetail.set(result.detail)
                                }
                                break
                            }
                            tried.incrementAndGet()
                        }
                    }
                }
            }

            var lastEmitted = -1L
            while (!stopFlag.get() && tried.get() < totalCount && isActive) {
                val current = tried.get()
                if (current != lastEmitted) {
                    emit(progressOf(current, totalCount, startNanos))
                    lastEmitted = current
                }
                delay(progressIntervalMs)
            }
        }

        val password = foundPassword.get()
        if (password != null) {
            emit(CrackingProgress.Found(password = password, matchedDetail = foundDetail.get()))
        } else {
            emit(CrackingProgress.Failed(CrackFailure.PasswordNotFound))
        }
    }

    private fun takeNextBatch(iterator: Iterator<String>, lock: Any, size: Int = BATCH_SIZE): List<String>? {
        synchronized(lock) {
            if (!iterator.hasNext()) return null
            val batch = ArrayList<String>(size)
            while (batch.size < size && iterator.hasNext()) {
                batch.add(iterator.next())
            }
            return batch
        }
    }

    private fun progressOf(current: Long, total: Long, startNanos: Long): CrackingProgress.Trying {
        val elapsedSeconds = (System.nanoTime() - startNanos) / 1_000_000_000.0
        val speed = if (elapsedSeconds > 0) (current / elapsedSeconds).toInt() else 0
        val remaining = (total - current).coerceAtLeast(0)
        val eta = if (speed > 0) remaining / speed else 0
        return CrackingProgress.Trying(
            triedCount = current,
            totalCount = total,
            speedPerSecond = speed,
            etaSeconds = eta
        )
    }

    companion object {
        const val BATCH_SIZE = 64
        const val PROGRESS_INTERVAL_MS = 350L
    }
}
