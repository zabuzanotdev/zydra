package com.zydra.domain.usecase

import com.zydra.domain.cracker.TesterFactory
import com.zydra.domain.cracker.TesterResult
import com.zydra.domain.engine.ParallelCrackEngine
import com.zydra.domain.model.CrackFailure
import com.zydra.domain.model.CrackTarget
import com.zydra.domain.model.CrackingProgress
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File

class CrackWithDictionaryUseCase(
    private val testerFactory: TesterFactory,
    private val engine: ParallelCrackEngine
) {

    fun invoke(target: CrackTarget, targetFile: File, passwords: List<String>): Flow<CrackingProgress> = flow {
        if (target is CrackTarget.Unsupported) {
            emit(CrackingProgress.Failed(CrackFailure.UnsupportedFormat(target.displayName)))
            return@flow
        }
        if (passwords.isEmpty()) {
            emit(CrackingProgress.Failed(CrackFailure.EmptyWordlist))
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
                    engine.crack(
                        candidates = passwords.asSequence(),
                        totalCount = passwords.size.toLong(),
                        tester = tester
                    ).collect { emit(it) }
                } finally {
                    tester.close()
                }
            }
        }
    }
}
