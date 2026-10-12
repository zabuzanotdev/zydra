package com.zydra.domain.usecase

import com.zydra.domain.model.CrackTarget
import java.util.Locale

class DetectTargetUseCase {

    fun invoke(displayName: String): CrackTarget {
        val extension = displayName.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when {
            extension == "zip" -> CrackTarget.Zip(displayName)
            extension == "pdf" -> CrackTarget.Pdf(displayName)
            extension == "rar" -> CrackTarget.Rar(displayName)
            displayName.contains("shadow", ignoreCase = true) -> CrackTarget.Shadow(displayName)
            else -> CrackTarget.Unsupported(displayName)
        }
    }
}
