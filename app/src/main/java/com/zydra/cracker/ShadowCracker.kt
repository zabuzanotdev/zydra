package com.zydra.cracker

import org.apache.commons.codec.digest.Crypt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Shadow Cracker dengan standarisasi ICracker.
 */
class ShadowCracker {

    fun verifyPassword(password: String, fullHash: String): Boolean {
        return try {
            val computedHash = Crypt.crypt(password, fullHash)
            computedHash == fullHash
        } catch (e: Exception) {
            false
        }
    }

    fun parseShadowLine(line: String): ShadowEntry? {
        val parts = line.split(":")
        if (parts.size < 2) return null
        
        val user = parts[0]
        val fullHash = parts[1]
        
        if (fullHash.length < 3 || fullHash.startsWith("*") || fullHash.startsWith("!")) return null
        
        return ShadowEntry(user, fullHash)
    }

    /**
     * Cracking logic. Jika filePath sebenarnya adalah isi hash, gunakan langsung.
     * Jika berupa file, coba parse baris per baris (mendukung file /etc/shadow).
     */
    fun crackWithDictionary(target: String, wordlist: List<String>): Flow<CrackingProgress> = flow {
        // Cek apakah target adalah single hash atau path ke file shadow
        val isFile = java.io.File(target).exists()
        val hashesToCrack = if (isFile) {
            java.io.File(target).readLines().mapNotNull { parseShadowLine(it)?.fullHash }
        } else {
            listOf(target)
        }

        if (hashesToCrack.isEmpty()) {
            emit(CrackingProgress.NotFound)
            return@flow
        }

        val total = wordlist.size
        hashesToCrack.forEach { hash ->
            wordlist.forEachIndexed { index, password ->
                if (verifyPassword(password, hash)) {
                    emit(CrackingProgress.Found("[$hash] -> $password"))
                    return@flow
                }
                if (index % 100 == 0) {
                    emit(CrackingProgress.Trying("Cracking Shadow...", (index + 1).toFloat() / total))
                }
            }
        }
        emit(CrackingProgress.NotFound)
    }
}

data class ShadowEntry(
    val user: String,
    val fullHash: String
)
