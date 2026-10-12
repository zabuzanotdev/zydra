package com.zydra.cracker

import com.github.junrar.Archive
import com.github.junrar.rarfile.FileHeader
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class RarCracker(private val rarFilePath: String) {

    fun crackWithDictionary(wordlist: List<String>): Flow<CrackingProgress> = flow {
        val total = wordlist.size
        val processedCount = AtomicInteger(0)
        val isFound = AtomicBoolean(false)
        var foundPassword: String? = null

        val coreCount = Runtime.getRuntime().availableProcessors()
        val chunkSize = (total / coreCount).coerceAtLeast(10)

        coroutineScope {
            wordlist.chunked(chunkSize).forEach { chunk ->
                launch(Dispatchers.Default) {
                    val file = File(rarFilePath)
                    for (password in chunk) {
                        if (isFound.get() || !isActive) break

                        if (tryPassword(file, password)) {
                            if (isFound.compareAndSet(false, true)) {
                                foundPassword = password
                            }
                            break
                        }
                        processedCount.incrementAndGet()
                    }
                }
            }

            var lastCount = 0
            while (!isFound.get() && processedCount.get() < total && isActive) {
                val current = processedCount.get()
                if (current > lastCount) {
                    emit(CrackingProgress.Trying("RAR Decrypting...", current.toFloat() / total))
                    lastCount = current
                }
                delay(500)
            }
        }

        foundPassword?.let { emit(CrackingProgress.Found(it)) } ?: emit(CrackingProgress.NotFound)
    }

    private fun tryPassword(file: File, password: String): Boolean {
        return try {
            Archive(file, password).use { archive ->
                if (archive.isEncrypted) {
                    val header = archive.fileHeaders.firstOrNull() ?: return false
                    archive.getInputStream(header).use { it.read() }
                    true
                } else false
            }
        } catch (e: Exception) {
            false
        }
    }
}
