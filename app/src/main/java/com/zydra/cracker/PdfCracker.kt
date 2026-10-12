package com.zydra.cracker

import android.content.Context
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class PdfCracker(private val context: Context, private val pdfFilePath: String) {

    init {
        PDFBoxResourceLoader.init(context)
    }

    fun crackWithDictionary(wordlist: List<String>): Flow<CrackingProgress> = flow {
        val total = wordlist.size
        val processedCount = AtomicInteger(0)
        val isFound = AtomicBoolean(false)
        var foundPassword: String? = null

        coroutineScope {
            wordlist.chunked(50).forEach { chunk ->
                launch(Dispatchers.Default) {
                    val file = File(pdfFilePath)
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
                    emit(CrackingProgress.Trying("PDF Decrypting...", current.toFloat() / total))
                    lastCount = current
                }
                delay(500)
            }
        }

        foundPassword?.let { emit(CrackingProgress.Found(it)) } ?: emit(CrackingProgress.NotFound)
    }

    private fun tryPassword(file: File, password: String): Boolean {
        return try {
            PDDocument.load(file, password).use { true }
        } catch (e: InvalidPasswordException) {
            false
        } catch (e: Exception) {
            false
        }
    }
}
