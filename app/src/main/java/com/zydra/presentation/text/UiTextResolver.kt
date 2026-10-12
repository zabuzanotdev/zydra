package com.zydra.presentation.text

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.zydra.presentation.model.UiText

@Composable
fun uiTextString(text: UiText): String {
    return when (text) {
        is UiText.Resource -> if (text.args.isEmpty()) {
            stringResource(text.resId)
        } else {
            stringResource(text.resId, *text.args.toTypedArray())
        }
    }
}
