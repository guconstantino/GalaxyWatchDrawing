package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.guconstantino.watchdraw.data.DrawingSyncStatus

// Subtle status tints + a translucent backdrop so the cloud reads over any drawing.
private val CloudSynced = Color(0xFF8BD4A8)   // soft green
private val CloudPending = Color(0xFFE6C36A)  // soft amber
private val CloudFailed = Color(0xFFE69A9A)   // soft red
private val BadgeBackdrop = Color(0x66000000) // translucent dark circle

/**
 * Small cloud badge showing a drawing's Google Photos sync status.
 * Renders nothing when the status is [DrawingSyncStatus.NONE].
 */
@Composable
fun CloudStatusBadge(status: DrawingSyncStatus, modifier: Modifier = Modifier) {
    if (status == DrawingSyncStatus.NONE) return
    Box(
        modifier = modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(BadgeBackdrop),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            DrawingSyncStatus.SYNCED -> IconCloudDone(Modifier.size(16.dp), CloudSynced)
            DrawingSyncStatus.PENDING -> IconCloudUpload(Modifier.size(16.dp), CloudPending)
            DrawingSyncStatus.FAILED -> IconCloudOff(Modifier.size(16.dp), CloudFailed)
            DrawingSyncStatus.NONE -> {}
        }
    }
}
