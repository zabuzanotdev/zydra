package com.zydra.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zydra.cracker.CrackerFactory
import com.zydra.cracker.CrackingProgress
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class CrackerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CrackingProgress?>(CrackingProgress.Idle)
    val uiState = _uiState.asStateFlow()

    private var crackingJob: Job? = null

    fun startCracking(context: Context, fileUri: Uri, wordlistUri: Uri) {
        crackingJob?.cancel()

        crackingJob = viewModelScope.launch {
            _uiState.value = CrackingProgress.Trying("Analyzing data...", 0f)

            val fileName = getFileName(context, fileUri) ?: "unknown"
            val wordlist = loadWordlist(context, wordlistUri)

            if (wordlist.isEmpty()) {
                _uiState.value = CrackingProgress.NotFound
                return@launch
            }

            val filePath = prepareLocalFile(context, fileUri) ?: return@launch
            val factory = CrackerFactory(context)
            val cracker = factory.getCracker(fileName, filePath)

            if (cracker == null) {
                _uiState.value = CrackingProgress.NotFound // Format not supported
                return@launch
            }

            cracker.crackWithDictionary(wordlist).collect { progress ->
                _uiState.value = progress
            }
        }
    }

    private fun getFileName(context: Context, uri: Uri): String? {
        var name: String? = null
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private suspend fun loadWordlist(context: Context, uri: Uri): List<String> = withContext(Dispatchers.IO) {
        val words = mutableListOf<String>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line: String? = reader.readLine()
                    while (line != null) {
                        if (line.isNotBlank()) {
                            words.add(line)
                        }
                        line = reader.readLine()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        words
    }

    private suspend fun prepareLocalFile(context: Context, contentUri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val cacheFile = java.io.File(context.cacheDir, "temp_target")
            context.contentResolver.openInputStream(contentUri)?.use { input ->
                cacheFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            cacheFile.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun stopCracking() {
        crackingJob?.cancel()
        _uiState.value = CrackingProgress.Idle
    }
}
