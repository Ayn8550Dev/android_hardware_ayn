/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.ui

import android.R.attr.colorAccent
import android.R.attr.colorBackground
import android.R.attr.textColorPrimary
import android.R.attr.textColorSecondary
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

@Composable
fun AynComposeTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val colors = context.obtainStyledAttributes(
        intArrayOf(
            colorAccent,
            colorBackground,
            textColorPrimary,
            textColorSecondary,
            android.R.attr.colorError,
        ),
    )
    val accent = Color(colors.getColor(0, android.graphics.Color.BLUE))
    val background = Color(colors.getColor(1, android.graphics.Color.WHITE))
    val onSurface = Color(colors.getColor(2, android.graphics.Color.BLACK))
    val onSurfaceVariant = Color(colors.getColor(3, android.graphics.Color.DKGRAY))
    val error = Color(colors.getColor(4, android.graphics.Color.RED))
    colors.recycle()

    val colorScheme = if (isSystemInDarkTheme()) {
        darkColorScheme(
            primary = accent,
            background = background,
            surface = background,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
        )
    } else {
        lightColorScheme(
            primary = accent,
            background = background,
            surface = background,
            onSurface = onSurface,
            onSurfaceVariant = onSurfaceVariant,
            error = error,
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
