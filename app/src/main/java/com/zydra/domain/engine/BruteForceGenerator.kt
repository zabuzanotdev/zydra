package com.zydra.domain.engine

class BruteForceGenerator(
    private val chars: String,
    private val minLength: Int,
    private val maxLength: Int
) {

    init {
        require(chars.isNotEmpty()) { "charset" }
        require(minLength >= 1 && maxLength >= minLength) { "range" }
    }

    fun generate(): Sequence<String> = sequence {
        val alphabet = chars.toCharArray()
        for (length in minLength..maxLength) {
            val indices = IntArray(length)
            while (true) {
                val builder = StringBuilder(length)
                for (index in indices) builder.append(alphabet[index])
                yield(builder.toString())

                var position = length - 1
                while (position >= 0) {
                    indices[position]++
                    if (indices[position] < alphabet.size) break
                    indices[position] = 0
                    position--
                }
                if (position < 0) break
            }
        }
    }
}
