package com.zydra.data.cracker

import com.zydra.domain.cracker.MatchResult
import com.zydra.domain.cracker.SinglePasswordTester
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.encryption.InvalidPasswordException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PdfSingleTester(private val file: File) : SinglePasswordTester {

    override suspend fun matches(password: String): MatchResult = withContext(Dispatchers.IO) {
        try {
            PDDocument.load(file, password).use { MatchResult.Matched() }
        } catch (_: InvalidPasswordException) {
            MatchResult.NoMatch
        } catch (_: Exception) {
            MatchResult.NoMatch
        }
    }
}
