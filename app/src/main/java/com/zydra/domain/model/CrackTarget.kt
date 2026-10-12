package com.zydra.domain.model

sealed interface CrackTarget {

    val displayName: String

    data class Zip(override val displayName: String) : CrackTarget

    data class Pdf(override val displayName: String) : CrackTarget

    data class Rar(override val displayName: String) : CrackTarget

    data class Shadow(override val displayName: String) : CrackTarget

    data class Unsupported(override val displayName: String) : CrackTarget
}
