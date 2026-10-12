package com.zydra.cracker

class BruteForceGenerator(
    private val chars: String,
    private val minLen: Int,
    private val maxLen: Int
) {
    fun generate(): Sequence<String> = sequence {
        for (length in minLen..maxLen) {
            yieldAll(generateRecursive("", length))
        }
    }

    private fun generateRecursive(prefix: String, length: Int): Sequence<String> = sequence {
        if (length == 0) {
            yield(prefix)
            return@sequence
        }

        for (char in chars) {
            yieldAll(generateRecursive(prefix + char, length - 1))
        }
    }

    fun getTotalCombinations(): Long {
        var total: Long = 0
        val base = chars.length.toDouble()
        for (len in minLen..maxLen) {
            total += Math.pow(base, len.toDouble()).toLong()
        }
        return total
    }
}
