package com.zydra.domain.repository

import java.io.File
import java.io.InputStream

interface FileResolver {

    @Throws(StagingException::class)
    suspend fun resolve(displayName: String, openInput: () -> InputStream?): File

    class StagingException(message: String, cause: Throwable? = null) : Exception(message, cause)
}
