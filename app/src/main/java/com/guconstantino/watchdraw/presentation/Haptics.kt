package com.guconstantino.watchdraw.presentation

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

private fun vibrator(context: Context): Vibrator? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)
            ?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

/** Single short tick — used for the two-finger tap (undo). */
fun hapticUndo(context: Context) {
    val v = vibrator(context) ?: return
    v.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
}

/** Quick double tick — used for the two-finger double-tap (redo). */
fun hapticRedo(context: Context) {
    val v = vibrator(context) ?: return
    // timings: wait 0, vibrate 18ms, pause 50ms, vibrate 18ms (no repeat)
    v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 18, 50, 18), -1))
}
