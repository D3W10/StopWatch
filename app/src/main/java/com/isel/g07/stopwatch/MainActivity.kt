package com.isel.g07.stopwatch

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.isel.g07.stopwatch.ui.theme.StopWatchTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val TAG = "StopWatch"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    StopWatchTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) }
                )
            }
        ) { innerPadding ->
            var state: StopWatchState by rememberSaveable(saver = StopWatchState.Saver) {
                mutableStateOf(StopWatchState.Initial)
            }
            var text by rememberSaveable { mutableStateOf("00:00.000") }
            val stopWatch by remember { mutableStateOf(StopWatch()) }
            val coroutineScope = rememberCoroutineScope()

            LaunchedEffect(state) {
                when (state) {
                    is StopWatchState.Initial -> {
                        Log.d(TAG, "Timer reset")
                        stopWatch.reset()
                        text = stopWatch.toString()
                    }
                    is StopWatchState.Running -> {
                        Log.d(TAG, "Starting timer")
                        stopWatch.start()

                        coroutineScope.launch {
                            while (stopWatch.isRunning) {
                                delay(10L)
                                text = stopWatch.toString()
                            }
                        }
                    }
                    is StopWatchState.Stopped -> {
                        Log.d(TAG, "Timer stopped")
                        stopWatch.stop()
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(30.dp))
                Row {
                    Controls(
                        state = state,
                        onStart = {
                            state = StopWatchState.Running(System.currentTimeMillis())
                        },
                        onStop = {
                            state = StopWatchState.Stopped(System.currentTimeMillis())
                        },
                        onReset = {
                            state = StopWatchState.Initial
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun Controls(
    state: StopWatchState,
    onStart: () -> Unit = {},
    onStop: () -> Unit = {},
    onReset: () -> Unit = {}
) {
    val startBg = MaterialTheme.colorScheme.primary
    val stopBg = MaterialTheme.colorScheme.error

    Button({ if (state !is StopWatchState.Running) onStart() else onStop() }, colors = ButtonDefaults.buttonColors(containerColor = if (state !is StopWatchState.Running) startBg else stopBg)) {
        Text(stringResource(if (state !is StopWatchState.Running) R.string.start else R.string.stop))
    }
    Spacer(modifier = Modifier.width(15.dp))
    Button({ onReset() }, enabled = state !is StopWatchState.Initial) {
        Text(stringResource(R.string.reset))
    }
}

sealed interface StopWatchState {
    data object Initial : StopWatchState
    data class Running(val initial: Long) : StopWatchState
    data class Stopped(val initial: Long): StopWatchState

    companion object {
        val Saver: Saver<MutableState<StopWatchState>, List<Long>> = Saver(
            save = { toSave ->
                toSave.value.let { state ->
                    when (state) {
                        is Running -> listOf(1L, state.initial)
                        is Stopped -> listOf(0L, state.initial)
                        else -> emptyList()
                    }
                }
            },
            restore = { saved ->
                if (saved.isNotEmpty()) {
                    if (saved[0] == 1L)
                        mutableStateOf(Running(saved[1]))
                    else
                        mutableStateOf(Stopped(saved[1]))
                }
                else
                    mutableStateOf(Initial)
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    App()
}