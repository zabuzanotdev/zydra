package com.zydra.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zydra.cracker.CrackingProgress

// Theme Colors
val MatrixGreen = Color(0xFF00FF41)
val CyberBlack = Color(0xFF0D0D0D)
val CyberGray = Color(0xFF1A1A1A)
val GlassWhite = Color(0x1AFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrackerScreen(
    onStartCracking: (fileUri: Uri, wordlistUri: Uri) -> Unit,
    progress: CrackingProgress?
) {
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var selectedWordlistUri by remember { mutableStateOf<Uri?>(null) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedFileUri = it
    }
    val wordlistPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        selectedWordlistUri = it
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            HeaderSection()

            SelectionCard(
                title = "TARGET ENCRYPTED FILE",
                icon = Icons.Default.Lock,
                selectedName = selectedFileUri?.path?.split("/")?.lastOrNull() ?: "No file selected",
                onClick = { filePicker.launch("*/*") }
            )

            SelectionCard(
                title = "WORDLIST (.TXT)",
                icon = Icons.Default.Info,
                selectedName = selectedWordlistUri?.path?.split("/")?.lastOrNull() ?: "No wordlist selected",
                onClick = { wordlistPicker.launch("text/plain") }
            )

            Button(
                onClick = { 
                    if (selectedFileUri != null && selectedWordlistUri != null) {
                        onStartCracking(selectedFileUri!!, selectedWordlistUri!!)
                    }
                },
                enabled = selectedFileUri != null && selectedWordlistUri != null && progress !is CrackingProgress.Trying,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(12.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MatrixGreen,
                    contentColor = Color.Black,
                    disabledContainerColor = CyberGray
                )
            ) {
                Icon(Icons.Default.Build, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("INITIATE DECRYPTION", fontWeight = FontWeight.Bold)
            }

            AnimatedVisibility(
                visible = progress != null && progress !is CrackingProgress.Idle,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                progress?.let { ProgressDashboard(it) }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Logo Section - Menggunakan Icon sistem sementara untuk stabilitas
        Surface(
            modifier = Modifier.size(64.dp).border(2.dp, MatrixGreen, RoundedCornerShape(8.dp)),
            color = Color.Transparent,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "Z", 
                    color = MatrixGreen, 
                    fontSize = 32.sp, 
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column {
            Text(
                "ZYDRA_CORE",
                color = MatrixGreen,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
            Text(
                "SYSTEM: ACTIVE",
                color = MatrixGreen.copy(alpha = 0.6f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectionCard(title: String, icon: ImageVector, selectedName: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = CyberGray,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().border(1.dp, GlassWhite, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, color = MatrixGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(selectedName, color = Color.White, fontSize = 14.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun ProgressDashboard(progress: CrackingProgress) {
    Surface(
        color = Color.Black,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MatrixGreen.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (progress) {
                is CrackingProgress.Trying -> {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DECRYPTING...", color = MatrixGreen, fontFamily = FontFamily.Monospace)
                        Text("${(progress.progress * 100).toInt()}%", color = MatrixGreen)
                    }
                    
                    LinearProgressIndicator(
                        progress = progress.progress,
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MatrixGreen,
                        trackColor = CyberGray
                    )

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        StatItem("SPEED", "${progress.speed} p/s")
                        StatItem("ETA", formatEta(progress.etaSeconds))
                    }
                }
                is CrackingProgress.Found -> {
                    Text("ACCESS GRANTED", color = MatrixGreen, fontWeight = FontWeight.Bold)
                    Text("KEY: ${progress.password}", color = Color.White, fontFamily = FontFamily.Monospace, fontSize = 20.sp)
                }
                is CrackingProgress.NotFound -> {
                    Text("ACCESS DENIED", color = Color.Red, fontWeight = FontWeight.Bold)
                    Text("No match found in wordlist.", color = Color.Gray)
                }
                else -> {}
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String) {
    Column {
        Text(label, color = Color.Gray, fontSize = 10.sp)
        Text(value, color = MatrixGreen, fontFamily = FontFamily.Monospace, fontSize = 14.sp)
    }
}

fun formatEta(seconds: Long): String {
    if (seconds <= 0) return "N/A"
    val h = seconds / 3600
    val m = (seconds % 3600) / 60
    val s = seconds % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
