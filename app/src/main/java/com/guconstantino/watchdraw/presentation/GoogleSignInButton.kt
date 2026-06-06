package com.guconstantino.watchdraw.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.Text

// Official "Sign in with Google" colors (Google branding guidelines).
private val GoogleButtonBg = Color(0xFFFFFFFF)
private val GoogleButtonText = Color(0xFF1F1F1F)
private val GoogleBlue = Color(0xFF4285F4)
private val GoogleGreen = Color(0xFF34A853)
private val GoogleYellow = Color(0xFFFBBC05)
private val GoogleRed = Color(0xFFEA4335)

// The four-color Google "G", as SVG paths on a 48x48 viewBox.
private const val PATH_BLUE =
    "M47.532 24.5528C47.532 22.9214 47.3997 21.2811 47.1175 19.6761H24.48V28.9181H37.4434C36.9055 31.8988 35.177 34.5356 32.6461 36.2111V42.2078H40.3801C44.9217 38.0278 47.532 31.8547 47.532 24.5528Z"
private const val PATH_GREEN =
    "M24.48 48.0016C30.9529 48.0016 36.4116 45.8764 40.3888 42.2078L32.6549 36.2111C30.5031 37.675 27.7252 38.5039 24.4888 38.5039C18.2275 38.5039 12.9187 34.2798 11.0139 28.6006H3.03296V34.7825C7.10718 42.8868 15.4056 48.0016 24.48 48.0016Z"
private const val PATH_YELLOW =
    "M11.0051 28.6006C9.99973 25.6199 9.99973 22.3922 11.0051 19.4115V13.2296H3.03298C-0.371021 20.0112 -0.371021 28.0009 3.03298 34.7825L11.0051 28.6006Z"
private const val PATH_RED =
    "M24.48 9.49932C27.9016 9.44641 31.2086 10.7339 33.6866 13.0973L40.5387 6.24523C36.2 2.17104 30.4414 -0.0584071 24.48 0.00161733C15.4056 0.00161733 7.10718 5.11644 3.03296 13.2296L11.0051 19.4115C12.9012 13.7235 18.2187 9.49932 24.48 9.49932Z"

/**
 * Official "Sign in with Google" button — white pill, four-color Google "G",
 * and the required label, per the Google branding guidelines. Sized to fit a
 * round Wear OS display.
 */
@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .heightIn(min = 44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(GoogleButtonBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        GoogleGLogo(Modifier.size(18.dp))
        Text(
            text = "Sign in with Google",
            color = GoogleButtonText,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun GoogleGLogo(modifier: Modifier = Modifier) {
    val parts: List<Pair<Path, Color>> = remember {
        listOf(
            PathParser().parsePathString(PATH_BLUE).toPath() to GoogleBlue,
            PathParser().parsePathString(PATH_GREEN).toPath() to GoogleGreen,
            PathParser().parsePathString(PATH_YELLOW).toPath() to GoogleYellow,
            PathParser().parsePathString(PATH_RED).toPath() to GoogleRed
        )
    }
    Canvas(modifier = modifier) {
        val s = size.minDimension / 48f
        scale(s, s, pivot = Offset.Zero) {
            parts.forEach { (path, color) -> drawPath(path, color) }
        }
    }
}
