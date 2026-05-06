/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun StickVisualizer(
    x: Float,
    y: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.size(120.dp)) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val radius = size.minDimension / 2f - 4.dp.toPx()

        // Outer circle
        drawCircle(
            color = Color.Gray,
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx()),
        )

        // Crosshair
        drawLine(
            color = Color.DarkGray,
            start = Offset(cx - radius, cy),
            end = Offset(cx + radius, cy),
            strokeWidth = 1.dp.toPx(),
        )
        drawLine(
            color = Color.DarkGray,
            start = Offset(cx, cy - radius),
            end = Offset(cx, cy + radius),
            strokeWidth = 1.dp.toPx(),
        )

        // Clamp the dot inside the circle (diagonals would overshoot otherwise)
        val dotRadius = 8.dp.toPx()
        val maxTravel = radius - dotRadius
        val rawDist = kotlin.math.sqrt(x * x + y * y).coerceAtLeast(Float.MIN_VALUE)
        val scale = if (rawDist > 1f) 1f / rawDist else 1f
        val dotX = cx + x * scale * maxTravel
        val dotY = cy + y * scale * maxTravel
        drawCircle(
            color = Color.Green,
            radius = dotRadius,
            center = Offset(dotX, dotY),
        )
    }
}
