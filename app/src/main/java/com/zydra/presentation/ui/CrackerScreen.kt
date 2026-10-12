package com.zydra.presentation.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zydra.R
import com.zydra.domain.model.CharsetCatalog
import com.zydra.domain.model.CharsetKey
import com.zydra.presentation.file.queryDisplayName
import com.zydra.presentation.model.BruteForceOptions
import com.zydra.presentation.model.CrackMode
import com.zydra.presentation.model.CrackerUiState
import com.zydra.presentation.model.UiText
import com.zydra.presentation.text.uiTextString
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CrackerScreen(
    state: CrackerUiState,
    onStartDictionary: (targetUri: Uri, targetName: String, wordlistUri: Uri) -> Unit,
    onStartBruteForce: (targetUri: Uri, targetName: String, options: BruteForceOptions) -> Unit,
    onStop: () -> Unit
) {
    val context = LocalContext.current
    val matrixGreen = colorResource(R.color.matrix_green)
    val cyberBlack = colorResource(R.color.cyber_black)
    val cyberGray = colorResource(R.color.cyber_gray)

    var mode by remember { mutableStateOf(CrackMode.DICTIONARY) }
    var targetUri by remember { mutableStateOf<Uri?>(null) }
    var targetName by remember { mutableStateOf("") }
    var wordlistUri by remember { mutableStateOf<Uri?>(null) }
    var wordlistName by remember { mutableStateOf("") }
    var charsetKeys by remember {
        mutableStateOf(setOf(CharsetKey.LOWERCASE, CharsetKey.DIGITS))
    }
    var minLengthText by remember { mutableStateOf("1") }
    var maxLengthText by remember { mutableStateOf("4") }

    val targetPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            targetUri = it
            targetName = queryDisplayName(context, it)
        }
    }
    val wordlistPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            wordlistUri = it
            wordlistName = queryDisplayName(context, it)
        }
    }

    val busy = state is CrackerUiState.Analyzing || state is CrackerUiState.Cracking
    val minLength = minLengthText.toIntOrNull()
    val maxLength = maxLengthText.toIntOrNull()
    val combinations = remember(charsetKeys, minLength, maxLength) {
        if (minLength != null && maxLength != null && charsetKeys.isNotEmpty()) {
            runCatching {
                CharsetCatalog.totalCombinations(
                    CharsetCatalog.charsFor(charsetKeys),
                    minLength,
                    maxLength
                )
            }.getOrNull()
        } else null
    }
    val canStartDictionary = targetUri != null && wordlistUri != null && !busy
    val canStartBruteForce = targetUri != null && !busy &&
        charsetKeys.isNotEmpty() && minLength != null && maxLength != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(cyberBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            HeaderSection(matrixGreen)

            ModeTabs(mode = mode, onModeChange = { mode = it }, accent = matrixGreen)

            SelectionCard(
                title = stringResource(R.string.target_title),
                icon = Icons.Default.Lock,
                selectedName = targetName.ifBlank { stringResource(R.string.target_empty) },
                onClick = { targetPicker.launch("*/*") },
                enabled = !busy,
                accent = matrixGreen,
                surface = cyberGray
            )

            if (mode == CrackMode.DICTIONARY) {
                SelectionCard(
                    title = stringResource(R.string.wordlist_title),
                    icon = Icons.Default.Info,
                    selectedName = wordlistName.ifBlank { stringResource(R.string.wordlist_empty) },
                    onClick = { wordlistPicker.launch("text/plain") },
                    enabled = !busy,
                    accent = matrixGreen,
                    surface = cyberGray
                )
            } else {
                CharsetSection(
                    selected = charsetKeys,
                    onToggle = { key ->
                        charsetKeys = if (key in charsetKeys) {
                            charsetKeys - key
                        } else {
                            charsetKeys + key
                        }
                    },
                    enabled = !busy,
                    accent = matrixGreen
                )
                LengthSection(
                    minText = minLengthText,
                    maxText = maxLengthText,
                    onMinChange = { minLengthText = it.filter(Char::isDigit).take(1) },
                    onMaxChange = { maxLengthText = it.filter(Char::isDigit).take(1) },
                    enabled = !busy,
                    accent = matrixGreen
                )
                combinations?.let {
                    Text(
                        text = stringResource(
                            R.string.bruteforce_combinations,
                            NumberFormat.getInstance(Locale.US).format(it)
                        ),
                        color = matrixGreen.copy(alpha = 0.7f),
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            if (busy) {
                OutlinedButton(
                    onClick = onStop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = matrixGreen),
                    border = androidx.compose.foundation.BorderStroke(1.dp, matrixGreen)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.action_cancel),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = {
                        val uri = targetUri ?: return@Button
                        if (mode == CrackMode.DICTIONARY) {
                            val wordUri = wordlistUri ?: return@Button
                            onStartDictionary(uri, targetName, wordUri)
                        } else {
                            if (minLength == null || maxLength == null) return@Button
                            onStartBruteForce(
                                uri,
                                targetName,
                                BruteForceOptions(charsetKeys, minLength, maxLength)
                            )
                        }
                    },
                    enabled = if (mode == CrackMode.DICTIONARY) {
                        canStartDictionary
                    } else {
                        canStartBruteForce
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = matrixGreen,
                        contentColor = Color.Black,
                        disabledContainerColor = cyberGray
                    )
                ) {
                    Icon(Icons.Default.Build, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        stringResource(R.string.action_start),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            AnimatedVisibility(
                visible = state !is CrackerUiState.Idle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                StatusDashboard(state = state, accent = matrixGreen, surface = cyberGray)
            }
        }
    }
}

@Composable
private fun HeaderSection(accent: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            modifier = Modifier
                .size(64.dp)
                .border(2.dp, accent, RoundedCornerShape(8.dp)),
            color = Color.Transparent,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = stringResource(R.string.app_logo),
                    color = accent,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(Modifier.width(16.dp))

        Column {
            Text(
                text = stringResource(R.string.app_tagline),
                color = accent,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                text = stringResource(R.string.app_status_active),
                color = accent.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun ModeTabs(mode: CrackMode, onModeChange: (CrackMode) -> Unit, accent: Color) {
    val tabs = listOf(
        CrackMode.DICTIONARY to stringResource(R.string.mode_dictionary),
        CrackMode.BRUTE_FORCE to stringResource(R.string.mode_bruteforce)
    )
    TabRow(
        selectedTabIndex = if (mode == CrackMode.DICTIONARY) 0 else 1,
        containerColor = Color.Transparent,
        contentColor = accent,
        indicator = { positions ->
            TabRowDefaults.Indicator(
                Modifier.tabIndicatorOffset(positions[if (mode == CrackMode.DICTIONARY) 0 else 1]),
                color = accent
            )
        }
    ) {
        tabs.forEachIndexed { index, (value, label) ->
            Tab(
                selected = (index == 0) == (mode == CrackMode.DICTIONARY),
                onClick = { onModeChange(value) },
                text = {
                    Text(
                        label,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                },
                selectedContentColor = accent,
                unselectedContentColor = Color.Gray
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionCard(
    title: String,
    icon: ImageVector,
    selectedName: String,
    onClick: () -> Unit,
    enabled: Boolean,
    accent: Color,
    surface: Color
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        color = surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colorResource(R.color.glass_white), RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = accent, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(selectedName, color = Color.White, fontSize = 14.sp, maxLines = 1)
            }
        }
    }
}

@Composable
private fun CharsetSection(
    selected: Set<CharsetKey>,
    onToggle: (CharsetKey) -> Unit,
    enabled: Boolean,
    accent: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = stringResource(R.string.charset_title),
            color = accent,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
        CharsetKey.values().forEach { key ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = key in selected,
                    onCheckedChange = { onToggle(key) },
                    enabled = enabled,
                    colors = CheckboxDefaults.colors(
                        checkedColor = accent,
                        checkmarkColor = Color.Black
                    )
                )
                Text(
                    text = when (key) {
                        CharsetKey.LOWERCASE -> stringResource(R.string.charset_lowercase)
                        CharsetKey.UPPERCASE -> stringResource(R.string.charset_uppercase)
                        CharsetKey.DIGITS -> stringResource(R.string.charset_digits)
                        CharsetKey.SYMBOLS -> stringResource(R.string.charset_symbols)
                        CharsetKey.SPACE -> stringResource(R.string.charset_space)
                    },
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun LengthSection(
    minText: String,
    maxText: String,
    onMinChange: (String) -> Unit,
    onMaxChange: (String) -> Unit,
    enabled: Boolean,
    accent: Color
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = accent,
        unfocusedBorderColor = Color.Gray,
        focusedLabelColor = accent,
        unfocusedLabelColor = Color.Gray,
        cursorColor = accent
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = minText,
            onValueChange = onMinChange,
            label = { Text(stringResource(R.string.field_min_length)) },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors,
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = maxText,
            onValueChange = onMaxChange,
            label = { Text(stringResource(R.string.field_max_length)) },
            enabled = enabled,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = fieldColors,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusDashboard(state: CrackerUiState, accent: Color, surface: Color) {
    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, accent.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (state) {
                CrackerUiState.Idle -> Unit
                CrackerUiState.Analyzing -> {
                    Text(
                        text = stringResource(R.string.status_analyzing),
                        color = accent,
                        fontFamily = FontFamily.Monospace
                    )
                }
                is CrackerUiState.Cracking -> {
                    val status = uiTextString(UiText.statusFor(state.target))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(status, color = accent, fontFamily = FontFamily.Monospace)
                        Text(
                            text = stringResource(
                                R.string.progress_percent,
                                (state.progress * 100).toInt()
                            ),
                            color = accent
                        )
                    }

                    LinearProgressIndicator(
                        progress = state.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = accent,
                        trackColor = surface
                    )

                    Text(
                        text = stringResource(
                            R.string.value_tried,
                            NumberFormat.getInstance(Locale.US).format(state.triedCount),
                            NumberFormat.getInstance(Locale.US).format(state.totalCount)
                        ),
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem(
                            stringResource(R.string.label_speed),
                            stringResource(R.string.value_speed, state.speedPerSecond)
                        )
                        StatItem(stringResource(R.string.label_eta), formatEta(state.etaSeconds))
                    }
                }
                is CrackerUiState.Success -> {
                    Text(
                        text = stringResource(R.string.result_granted),
                        color = accent,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stringResource(R.string.result_key, state.password),
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 20.sp
                    )
                    state.matchedDetail?.let { detail ->
                        Text(
                            text = stringResource(R.string.result_detail, detail),
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
                is CrackerUiState.Error -> {
                    Text(
                        text = stringResource(R.string.result_denied),
                        color = Color.Red,
                        fontWeight = FontWeight.Bold
                    )
                    Text(uiTextString(state.message), color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(
            value,
            color = colorResource(R.color.matrix_green),
            fontFamily = FontFamily.Monospace,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun formatEta(seconds: Long): String {
    if (seconds <= 0) return stringResource(R.string.value_eta_unknown)
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60
    return if (hours > 0) {
        "%02d:%02d:%02d".format(hours, minutes, secs)
    } else {
        "%02d:%02d".format(minutes, secs)
    }
}
