/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier

@Composable
fun CalibrationScreen(
    viewModel: CalibrationViewModel,
    onFinish: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        when (val phase = state.phase) {
            CalibrationPhase.Intro -> PhaseIntroScreen(
                onStart = { viewModel.startCalibration() },
                onCancel = onFinish,
            )

            CalibrationPhase.Center,
            CalibrationPhase.RangeLeft,
            CalibrationPhase.RangeRight,
            CalibrationPhase.DeadzoneLeft,
            CalibrationPhase.DeadzoneRight -> CalibrationPhaseScreen(
                phase = phase,
                state = state,
                onNext = { viewModel.advancePhase() },
            )

            CalibrationPhase.Test -> TestScreen(
                state = state,
                onAccept = {
                    viewModel.acceptCalibration()
                    onFinish()
                },
                onRetry = { viewModel.retryCalibration() },
            )
        }
    }
}
