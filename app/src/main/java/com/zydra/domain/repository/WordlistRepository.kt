package com.zydra.domain.repository

import java.io.InputStream

interface WordlistRepository {

    @Throws(WordlistException::class)
    suspend fun readPasswords(openInput: () -> InputStream?): List<String>

    sealed class WordlistException(message: String) : Exception(message) {
        class Empty : WordlistException("empty")
        class TooLarge(val maxAllowed: Int) : WordlistException("too large")
        class Unreadable(cause: Throwable) : WordlistException("unreadable")
    }
}
