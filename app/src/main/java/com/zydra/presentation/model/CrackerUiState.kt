package com.zydra.presentation.model

import androidx.annotation.StringRes
import com.zydra.R
import com.zydra.domain.model.CharsetKey
import com.zydra.domain.model.CrackTarget

sealed interface UiText {

    data class Resource(@StringRes val resId: Int, val args: List<String> = emptyList()) : UiText

    companion object {

        fun statusFor(target: CrackTarget?): UiText = when (target) {
            is CrackTarget.Zip -> Resource(R.string.status_cracking_zip)
            is CrackTarget.Pdf -> Resource(R.string.status_cracking_pdf)
            is CrackTarget.Rar -> Resource(R.string.status_cracking_rar)
            is CrackTarget.Shadow -> Resource(R.string.status_cracking_shadow)
            else -> Resource(R.string.status_decrypting)
        }
    }
}

enum class CrackMode {
    DICTIONARY,
    BRUTE_FORCE
}

data class BruteForceOptions(
    val charsetKeys: Set<CharsetKey> = setOf(CharsetKey.LOWERCASE, CharsetKey.DIGITS),
    val minLength: Int = 1,
    val maxLength: Int = 4
)

sealed interface CrackerUiState {

    data object Idle : CrackerUiState

    data object Analyzing : CrackerUiState

    data class Cracking(
        val target: CrackTarget?,
        val triedCount: Long,
        val totalCount: Long,
        val progress: Float,
        val speedPerSecond: Int,
        val etaSeconds: Long
    ) : CrackerUiState

    data class Success(
        val password: String,
        val matchedDetail: String? = null
    ) : CrackerUiState

    data class Error(val message: UiText) : CrackerUiState
}
