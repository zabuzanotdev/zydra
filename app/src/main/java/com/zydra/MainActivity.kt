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
import com.zydra.data.di.AppContainer
import com.zydra.presentation.CrackerViewModel
import com.zydra.presentation.ui.CrackerScreen

class MainActivity : ComponentActivity() {

    private val appContainer: AppContainer by lazy { AppContainer(applicationContext) }

    private val viewModel: CrackerViewModel by viewModels {
        CrackerViewModel.Factory(application, appContainer)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val state by viewModel.uiState.collectAsState()

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CrackerScreen(
                        state = state,
                        onStartDictionary = { targetUri, targetName, wordlistUri ->
                            viewModel.startDictionaryCracking(targetUri, targetName, wordlistUri)
                        },
                        onStartBruteForce = { targetUri, targetName, options ->
                            viewModel.startBruteForceCracking(targetUri, targetName, options)
                        },
                        onStop = { viewModel.stopCracking() }
                    )
                }
            }
        }
    }
}
