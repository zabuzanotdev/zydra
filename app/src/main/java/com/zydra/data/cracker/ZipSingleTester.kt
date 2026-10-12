package com.zydra.data.cracker

import com.zydra.domain.cracker.MatchResult
import com.zydra.domain.cracker.SinglePasswordTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile
import java.io.File

class ZipSingleTester(private val file: File) : SinglePasswordTester {

    override suspend fun matches(password: String): MatchResult = withContext(Dispatchers.IO) {
        try {
            val probe = ZipFile(file)
            if (!probe.isEncrypted) return@withContext MatchResult.NoMatch
            probe.setPassword(password.toCharArray())
            val header = probe.fileHeaders.firstOrNull() ?: return@withContext MatchResult.NoMatch
            probe.getInputStream(header).use { it.read() }
            MatchResult.Matched()
        } catch (_: Exception) {
            MatchResult.NoMatch
        }
    }
}
