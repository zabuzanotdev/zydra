package com.zydra.data.file

import android.content.Context
import com.zydra.domain.repository.FileResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

class CacheFileResolver(private val context: Context) : FileResolver {

    override suspend fun resolve(displayName: String, openInput: () -> InputStream?): File =
        withContext(Dispatchers.IO) {
            val input = openInput()
                ?: throw FileResolver.StagingException("Cannot open target file")
            val directory = File(context.cacheDir, TARGET_DIRECTORY).apply { mkdirs() }
            pruneStaleFiles(directory)
            val safeName = "target_${System.currentTimeMillis()}_${sanitize(displayName)}"
            val output = File(directory, safeName)
            try {
                input.use { source ->
                    output.outputStream().use { destination ->
                        source.copyTo(destination)
                    }
                }
            } catch (cause: Exception) {
                output.delete()
                throw FileResolver.StagingException("Cannot stage target file", cause)
            }
            output
        }

    private fun pruneStaleFiles(directory: File) {
        val cutoff = System.currentTimeMillis() - STALE_AFTER_MS
        directory.listFiles()?.forEach { file ->
            if (file.lastModified() < cutoff) file.delete()
        }
    }

    private fun sanitize(name: String): String {
        val base = name.substringAfterLast('/').substringAfterLast('\\').ifBlank { "file" }
        return base.replace(Regex("[^A-Za-z0-9._-]"), "_").takeLast(MAX_NAME_LENGTH)
    }

    companion object {
        private const val TARGET_DIRECTORY = "zydra_targets"
        private const val STALE_AFTER_MS = 24 * 60 * 60 * 1000L
        private const val MAX_NAME_LENGTH = 80
    }
}
