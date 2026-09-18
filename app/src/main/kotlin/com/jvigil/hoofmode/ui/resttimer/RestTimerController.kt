package com.jvigil.hoofmode.ui.resttimer

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RestTimerState {
    data object Idle : RestTimerState
    data class Running(val secondsRemaining: Int, val totalSeconds: Int) : RestTimerState
}

/** A simple countdown used between sets. One instance per active session screen. */
class RestTimerController(private val context: Context) {

    private val _state = MutableStateFlow<RestTimerState>(RestTimerState.Idle)
    val state: StateFlow<RestTimerState> = _state.asStateFlow()

    private var job: Job? = null

    fun start(scope: CoroutineScope, totalSeconds: Int, vibrateOnFinish: Boolean) {
        job?.cancel()
        job = scope.launch {
            var remaining = totalSeconds
            while (remaining >= 0) {
                _state.value = RestTimerState.Running(remaining, totalSeconds)
                delay(1000)
                remaining--
            }
            _state.value = RestTimerState.Idle
            if (vibrateOnFinish) vibrate()
        }
    }

    fun cancel() {
        job?.cancel()
        _state.value = RestTimerState.Idle
    }

    private fun vibrate() {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        vibrator?.vibrate(VibrationEffect.createOneShot(400, VibrationEffect.DEFAULT_AMPLITUDE))
    }
}
