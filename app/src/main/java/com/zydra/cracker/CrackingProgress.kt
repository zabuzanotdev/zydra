package com.zydra.cracker

sealed class CrackingProgress {
    data class Trying(
        val password: String,
        val progress: Float,
        val speed: Int = 0, // passwords per second
        val etaSeconds: Long = 0
    ) : CrackingProgress()

    data class Found(val password: String) : CrackingProgress()
    object NotFound : CrackingProgress()
    object Idle : CrackingProgress()
}
