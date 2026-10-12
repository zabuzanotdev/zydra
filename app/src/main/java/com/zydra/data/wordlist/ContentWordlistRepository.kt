package com.zydra.data.wordlist

import com.zydra.domain.repository.WordlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

class ContentWordlistRepository : WordlistRepository {

    override suspend fun readPasswords(openInput: () -> InputStream?): List<String> =
        withContext(Dispatchers.IO) {
            val input = openInput()
                ?: throw WordlistRepository.WordlistException.Unreadable(
                    IllegalStateException("Cannot open wordlist")
                )
            try {
                val passwords = ArrayList<String>(8192)
                BufferedReader(InputStreamReader(input)).use { reader ->
                    var line = reader.readLine()
                    while (line != null) {
                        val candidate = line.trim()
                        if (candidate.isNotEmpty()) {
                            passwords.add(candidate)
                            if (passwords.size > MAX_WORDLIST_LINES) {
                                throw WordlistRepository.WordlistException.TooLarge(MAX_WORDLIST_LINES)
                            }
                        }
                        line = reader.readLine()
                    }
                }
                if (passwords.isEmpty()) throw WordlistRepository.WordlistException.Empty()
                passwords
            } catch (error: WordlistRepository.WordlistException) {
                throw error
            } catch (error: Exception) {
                throw WordlistRepository.WordlistException.Unreadable(error)
            }
        }

    companion object {
        const val MAX_WORDLIST_LINES = 500_000
    }
}
