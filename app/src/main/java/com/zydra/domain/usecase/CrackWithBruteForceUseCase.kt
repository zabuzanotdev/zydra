package com.zydra.domain.usecase

import com.zydra.domain.cracker.TesterFactory
import com.zydra.domain.cracker.TesterResult
import com.zydra.domain.engine.BruteForceGenerator
import com.zydra.domain.engine.ParallelCrackEngine
import com.zydra.domain.model.CharsetCatalog
import com.zydra.domain.model.CharsetKey
import com.zydra.domain.model.CrackFailure
import com.zydra.domain.model.CrackTarget
import com.zydra.domain.model.CrackingProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class CrackWithBruteForceUseCase(
    private val testerFactory: TesterFactory,
    private val engine: ParallelCrackEngine
) {

    fun invoke(
        target: CrackTarget,
        targetFile: File,
        charsetKeys: Set<CharsetKey>,
        minLength: Int,
        maxLength: Int
    ): Flow<CrackingProgress> = flow {
        if (target is CrackTarget.Unsupported) {
            emit(CrackingProgress.Failed(CrackFailure.UnsupportedFormat(target.displayName)))
            return@flow
        }
        val chars = CharsetCatalog.charsFor(charsetKeys)
        if (chars.isEmpty()) {
            emit(CrackingProgress.Failed(CrackFailure.NoCharsetSelected))
            return@flow
        }
        if (minLength < 1 || maxLength < minLength || maxLength > MAX_LENGTH) {
            emit(CrackingProgress.Failed(CrackFailure.InvalidLengthRange))
            return@flow
        }
        val total = CharsetCatalog.totalCombinations(chars, minLength, maxLength)
        if (total > MAX_COMBINATIONS) {
            emit(CrackingProgress.Failed(CrackFailure.TooManyCombinations(total)))
            return@flow
        }
        val result = testerFactory.testerFor(target, targetFile)
        when (result) {
            TesterResult.Rar5NotSupported -> {
                emit(CrackingProgress.Failed(CrackFailure.Rar5NotSupported))
                return@flow
            }
            TesterResult.Unavailable -> {
                emit(CrackingProgress.Failed(CrackFailure.UnsupportedFormat(target.displayName)))
                return@flow
            }
            is TesterResult.Available -> {
                val tester = result.tester
                try {
                    val generator = BruteForceGenerator(chars, minLength, maxLength)
                    engine.crack(
                        candidates = generator.generate(),
                        totalCount = total,
                        tester = tester
                    ).collect { emit(it) }
                } finally {
                    tester.close()
                }
            }
        }
    }

    companion object {
        const val MAX_LENGTH = 6
        const val MAX_COMBINATIONS = 20_000_000L
    }
}
