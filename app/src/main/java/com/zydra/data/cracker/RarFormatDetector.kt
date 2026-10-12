package com.zydra.data.cracker

import java.io.File
import java.io.RandomAccessFile

object RarFormatDetector {

    private val RAR4_SIGNATURE = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x00)
    private val RAR5_SIGNATURE = byteArrayOf(0x52, 0x61, 0x72, 0x21, 0x1A, 0x07, 0x01, 0x00)

    fun isRar5(file: File): Boolean {
        return try {
            RandomAccessFile(file, "r").use { access ->
                val header = ByteArray(RAR5_SIGNATURE.size)
                if (access.read(header) != header.size) return false
                header.contentEquals(RAR5_SIGNATURE)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isRar4(file: File): Boolean {
        return try {
            RandomAccessFile(file, "r").use { access ->
                val header = ByteArray(RAR4_SIGNATURE.size)
                if (access.read(header) != header.size) return false
                header.contentEquals(RAR4_SIGNATURE)
            }
        } catch (_: Exception) {
            false
        }
    }
}
