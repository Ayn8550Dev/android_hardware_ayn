/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun TriggerBar(
    value: Float,
    label: String,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.width(32.dp).height(120.dp)) {
        val cornerRadius = CornerRadius(4.dp.toPx())

        // Background
        drawRoundRect(
            color = Color.Gray,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height),
            cornerRadius = cornerRadius,
            style = Stroke(width = 2.dp.toPx()),
        )

        // Fill (from bottom)
        val fillHeight = size.height * value.coerceIn(0f, 1f)
        if (fillHeight > 0f) {
            drawRoundRect(
                color = Color.Green,
                topLeft = Offset(0f, size.height - fillHeight),
                size = Size(size.width, fillHeight),
                cornerRadius = cornerRadius,
            )
        }
    }
}
