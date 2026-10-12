package com.zydra.data.cracker

import com.zydra.domain.cracker.MatchResult
import com.zydra.domain.cracker.SinglePasswordTester
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.Crypt
import java.io.File

class ShadowSingleTester private constructor(
    private val hashes: List<HashEntry>
) : SinglePasswordTester {

    override suspend fun matches(password: String): MatchResult = withContext(Dispatchers.Default) {
        for (entry in hashes) {
            val computed = try {
                Crypt.crypt(password, entry.fullHash)
            } catch (_: Exception) {
                continue
            }
            if (computed == entry.fullHash) {
                return@withContext MatchResult.Matched(entry.user)
            }
        }
        MatchResult.NoMatch
    }

    companion object {

        fun fromFile(file: File): ShadowSingleTester? {
            val entries = try {
                file.readLines().mapNotNull { parseLine(it) }
            } catch (_: Exception) {
                return null
            }
            if (entries.isEmpty()) return null
            return ShadowSingleTester(entries)
        }

        fun parseLine(line: String): HashEntry? {
            val parts = line.split(":")
            if (parts.size < 2) return null
            val user = parts[0].trim()
            val fullHash = parts[1].trim()
            if (user.isEmpty() || fullHash.length < 3) return null
            if (fullHash.startsWith("*") || fullHash.startsWith("!")) return null
            return HashEntry(user, fullHash)
        }
    }

    data class HashEntry(val user: String, val fullHash: String)
}
