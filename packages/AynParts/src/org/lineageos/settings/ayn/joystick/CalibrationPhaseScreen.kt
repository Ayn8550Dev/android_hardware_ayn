/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.lineageos.settings.ayn.R
import org.lineageos.settings.ayn.joystick.components.StickVisualizer
import org.lineageos.settings.ayn.joystick.components.TriggerBar

@Composable
fun CalibrationPhaseScreen(
    phase: CalibrationPhase,
    state: CalibrationUiState,
    onNext: () -> Unit,
) {
    val sample = state.currentSample

    val title = when (phase) {
        CalibrationPhase.Center -> stringResource(R.string.calibration_phase_center_title)
        CalibrationPhase.RangeLeft -> stringResource(R.string.calibration_phase_range_left_title)
        CalibrationPhase.RangeRight -> stringResource(R.string.calibration_phase_range_right_title)
        CalibrationPhase.DeadzoneLeft -> stringResource(R.string.calibration_phase_deadzone_left_title)
        CalibrationPhase.DeadzoneRight -> stringResource(R.string.calibration_phase_deadzone_right_title)
        else -> ""
    }

    val description = when (phase) {
        CalibrationPhase.Center -> stringResource(R.string.calibration_phase_center_desc)
        CalibrationPhase.RangeLeft -> stringResource(R.string.calibration_phase_range_left_desc)
        CalibrationPhase.RangeRight -> stringResource(R.string.calibration_phase_range_right_desc)
        CalibrationPhase.DeadzoneLeft -> stringResource(R.string.calibration_phase_deadzone_left_desc)
        CalibrationPhase.DeadzoneRight -> stringResource(R.string.calibration_phase_deadzone_right_desc)
        else -> ""
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (sample != null) {
            when (phase) {
                CalibrationPhase.Center -> Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    StickVisualizer(
                        x = JoystickMapping.normalizeAxis(sample.leftX),
                        y = JoystickMapping.normalizeAxis(sample.leftY),
                        label = "L",
                    )
                    StickVisualizer(
                        x = JoystickMapping.normalizeAxis(sample.rightX),
                        y = JoystickMapping.normalizeAxis(sample.rightY),
                        label = "R",
                    )
                }
                CalibrationPhase.RangeLeft,
                CalibrationPhase.DeadzoneLeft -> StickVisualizer(
                    x = JoystickMapping.normalizeAxis(sample.leftX),
                    y = JoystickMapping.normalizeAxis(sample.leftY),
                    label = "L",
                )
                CalibrationPhase.RangeRight,
                CalibrationPhase.DeadzoneRight -> StickVisualizer(
                    x = JoystickMapping.normalizeAxis(sample.rightX),
                    y = JoystickMapping.normalizeAxis(sample.rightY),
                    label = "R",
                )
                else -> {}
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNext,
            enabled = state.readyToAdvance,
        ) {
            Text(stringResource(R.string.calibration_next))
        }
    }
}
