package com.zydra.presentation.file

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns

fun queryDisplayName(context: Context, uri: Uri): String {
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) {
            return cursor.getString(nameIndex) ?: fallbackName(uri)
        }
    }
    return fallbackName(uri)
}

private fun fallbackName(uri: Uri): String {
    return uri.lastPathSegment?.substringAfterLast('/')?.ifBlank { null } ?: "file"
}
