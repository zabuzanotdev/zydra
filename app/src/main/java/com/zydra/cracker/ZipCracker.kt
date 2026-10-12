package com.zydra.cracker

import net.lingala.zip4j.ZipFile
import net.lingala.zip4j.exception.ZipException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ZipCracker(private val zipFilePath: String) {

    fun crackWithDictionary(wordlist: List<String>): Flow<CrackingProgress> = flow {
        val total = wordlist.size
        val foundPassword = MutableStateFlow<String?>(null)
        val processedCount = AtomicInteger(0)
        val isFound = AtomicBoolean(false)
        val startTime = System.currentTimeMillis()
        
        val coreCount = Runtime.getRuntime().availableProcessors()
        val chunkSize = (total / coreCount).coerceAtLeast(50)

        coroutineScope {
            wordlist.chunked(chunkSize).forEach { chunk ->
                launch(Dispatchers.Default) {
                    val zipFile = ZipFile(zipFilePath)
                    try {
                        for (password in chunk) {
                            if (isFound.get() || !isActive) break
                            
                            if (tryPassword(zipFile, password)) {
                                if (isFound.compareAndSet(false, true)) {
                                    foundPassword.value = password
                                }
                                break
                            }
                            processedCount.incrementAndGet()
                        }
                    } catch (e: Exception) {}
                }
            }

            var lastEmittedCount = 0
            while (!isFound.get() && processedCount.get() < total && isActive) {
                val currentCount = processedCount.get()
                if (currentCount > lastEmittedCount) {
                    val elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000.0
                    val speed = if (elapsedSeconds > 0) (currentCount / elapsedSeconds).toInt() else 0
                    val remaining = total - currentCount
                    val eta = if (speed > 0) (remaining / speed).toLong() else 0
                    
                    emit(CrackingProgress.Trying(
                        password = "Decrypting...",
                        progress = currentCount.toFloat() / total,
                        speed = speed,
                        etaSeconds = eta
                    ))
                    lastEmittedCount = currentCount
                }
                delay(400)
                if (isFound.get()) break
            }
        }

        val finalResult = foundPassword.value
        if (finalResult != null) {
            emit(CrackingProgress.Found(finalResult))
        } else {
            emit(CrackingProgress.NotFound)
        }
    }

    private fun tryPassword(zipFile: ZipFile, password: String): Boolean {
        return try {
            zipFile.setPassword(password.toCharArray())
            zipFile.fileHeaders.firstOrNull()?.let { header ->
                zipFile.getInputStream(header).use { it.read() }
                true
            } ?: false
        } catch (e: ZipException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
