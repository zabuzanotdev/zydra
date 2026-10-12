package com.zydra.data.cracker

import com.github.junrar.Archive
import com.zydra.domain.cracker.MatchResult
import com.zydra.domain.cracker.SinglePasswordTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class RarSingleTester(private val file: File) : SinglePasswordTester {

    override suspend fun matches(password: String): MatchResult = withContext(Dispatchers.IO) {
        try {
            Archive(file, password).use { archive ->
                if (!archive.isEncrypted) return@withContext MatchResult.NoMatch
                val header = archive.fileHeaders.firstOrNull() ?: return@withContext MatchResult.NoMatch
                archive.getInputStream(header).use { it.read() }
                MatchResult.Matched()
            }
        } catch (_: Exception) {
            MatchResult.NoMatch
        }
    }
}
