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

/** Very short, light detent — used when scrolling the gallery crown. */
fun hapticScrollTick(context: Context) {
    val v = vibrator(context) ?: return
    v.vibrate(VibrationEffect.createOneShot(12, 80))
}

/** Success feedback — used for restore or clear actions. */
fun hapticSuccess(context: Context) {
    val v = vibrator(context) ?: return
    // Wait 0, vibrate 30ms, pause 40ms, vibrate 30ms
    v.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 30), -1))
}

/** Warning/Alert feedback — used for delete or permanent delete. */
fun hapticWarning(context: Context) {
    val v = vibrator(context) ?: return
    // One longish, more intense vibration
    v.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE))
}
