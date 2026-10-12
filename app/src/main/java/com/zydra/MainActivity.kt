package com.zydra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.zydra.ui.CrackerScreen
import com.zydra.ui.CrackerViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: CrackerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val progressState by viewModel.uiState.collectAsState()
            
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrackerScreen(
                        onStartCracking = { fileUri, wordlistUri ->
                            viewModel.startCracking(this@MainActivity, fileUri, wordlistUri)
                        },
                        progress = progressState
                    )
                }
            }
        }
    }
}
