package com.zydra.domain.model

sealed interface CrackingProgress {

    data class Trying(
        val triedCount: Long,
        val totalCount: Long,
        val speedPerSecond: Int,
        val etaSeconds: Long
    ) : CrackingProgress {

        val fraction: Float
            get() = if (totalCount <= 0) 0f else (triedCount.toFloat() / totalCount).coerceIn(0f, 1f)
    }

    data class Found(
        val password: String,
        val matchedDetail: String? = null
    ) : CrackingProgress

    data class Failed(val failure: CrackFailure) : CrackingProgress
}
