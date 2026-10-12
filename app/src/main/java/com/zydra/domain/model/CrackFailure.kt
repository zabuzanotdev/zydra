package com.zydra.domain.model

sealed interface CrackFailure {

    data object PasswordNotFound : CrackFailure

    data class UnsupportedFormat(val displayName: String) : CrackFailure

    data object Rar5NotSupported : CrackFailure

    data object EmptyWordlist : CrackFailure

    data class WordlistTooLarge(val maxAllowed: Int) : CrackFailure

    data object FileUnreadable : CrackFailure

    data object NoCharsetSelected : CrackFailure

    data object InvalidLengthRange : CrackFailure

    data class TooManyCombinations(val total: Long) : CrackFailure

    data object NoCandidate : CrackFailure
}
