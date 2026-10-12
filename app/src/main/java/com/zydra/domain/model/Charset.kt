package com.zydra.domain.model

enum class CharsetKey {
    LOWERCASE,
    UPPERCASE,
    DIGITS,
    SYMBOLS,
    SPACE
}

object CharsetCatalog {

    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~"
    private const val SPACE = " "

    fun charsFor(keys: Set<CharsetKey>): String {
        val builder = StringBuilder()
        if (CharsetKey.LOWERCASE in keys) builder.append(LOWER)
        if (CharsetKey.UPPERCASE in keys) builder.append(UPPER)
        if (CharsetKey.DIGITS in keys) builder.append(DIGITS)
        if (CharsetKey.SYMBOLS in keys) builder.append(SYMBOLS)
        if (CharsetKey.SPACE in keys) builder.append(SPACE)
        return builder.toString()
    }

    fun totalCombinations(chars: String, minLength: Int, maxLength: Int): Long {
        require(minLength >= 1 && maxLength >= minLength) { "Invalid range" }
        var total = 0L
        val base = chars.length.toLong()
        for (length in minLength..maxLength) {
            var power = 1L
            repeat(length) {
                if (power > Long.MAX_VALUE / base) return Long.MAX_VALUE
                power *= base
            }
            if (total > Long.MAX_VALUE - power) return Long.MAX_VALUE
            total += power
        }
        return total
    }
}
