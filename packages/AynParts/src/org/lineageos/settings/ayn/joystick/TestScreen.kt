/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.lineageos.settings.ayn.R
import org.lineageos.settings.ayn.joystick.components.StickVisualizer

@Composable
fun TestScreen(
    state: CalibrationUiState,
    onAccept: () -> Unit,
    onRetry: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.calibration_test_title),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.calibration_test_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Before (raw)
            Text(
                text = stringResource(R.string.calibration_test_before),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StickVisualizer(
                    x = state.rawLeftX,
                    y = state.rawLeftY,
                    label = "L",
                )
                StickVisualizer(
                    x = state.rawRightX,
                    y = state.rawRightY,
                    label = "R",
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // After (calibrated)
            Text(
                text = stringResource(R.string.calibration_test_after),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                StickVisualizer(
                    x = state.mappedLeftX,
                    y = state.mappedLeftY,
                    label = "L",
                )
                StickVisualizer(
                    x = state.mappedRightX,
                    y = state.mappedRightY,
                    label = "R",
                )
            }
        }

        Row(
            modifier = Modifier.align(Alignment.BottomEnd),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(onClick = onRetry) {
                Text(stringResource(R.string.calibration_retry))
            }
            Button(onClick = onAccept) {
                Text(stringResource(R.string.calibration_accept))
            }
        }
    }
}
