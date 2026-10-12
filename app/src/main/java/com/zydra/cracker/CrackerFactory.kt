package com.zydra.cracker

import android.content.Context
import kotlinx.coroutines.flow.Flow
import java.util.Locale

interface ICracker {
    fun crackWithDictionary(wordlist: List<String>): Flow<CrackingProgress>
}

class CrackerFactory(private val context: Context) {

    fun getCracker(fileName: String, filePath: String): ICracker? {
        val extension = fileName.substringAfterLast('.', "").lowercase(Locale.ROOT)

        return when {
            extension == "zip" -> object : ICracker {
                override fun crackWithDictionary(wordlist: List<String>) = ZipCracker(filePath).crackWithDictionary(wordlist)
            }
            extension == "pdf" -> object : ICracker {
                override fun crackWithDictionary(wordlist: List<String>) = PdfCracker(context, filePath).crackWithDictionary(wordlist)
            }
            extension == "rar" -> object : ICracker {
                override fun crackWithDictionary(wordlist: List<String>) = RarCracker(filePath).crackWithDictionary(wordlist)
            }
            fileName.contains("shadow", ignoreCase = true) -> object : ICracker {
                override fun crackWithDictionary(wordlist: List<String>) = ShadowCracker().crackWithDictionary(filePath, wordlist)
            }
            else -> null
        }
    }
}
