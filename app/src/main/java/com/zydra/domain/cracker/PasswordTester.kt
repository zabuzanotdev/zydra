package com.zydra.domain.cracker

import com.zydra.domain.model.CrackTarget
import java.io.File

sealed interface MatchResult {
    data object NoMatch : MatchResult
    data class Matched(val detail: String? = null) : MatchResult
}

interface SinglePasswordTester {
    suspend fun matches(password: String): MatchResult
    fun close() {}
}

interface TesterFactory {
    fun testerFor(target: CrackTarget, file: File): TesterResult
}

sealed interface TesterResult {
    data class Available(val tester: SinglePasswordTester) : TesterResult
    data object Rar5NotSupported : TesterResult
    data object Unavailable : TesterResult
}
