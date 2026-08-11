/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalibrationViewModel(application: Application) : AndroidViewModel(application) {
    private val sampler = JoystickSampler()
    private var engine = CalibrationEngine()

    private val _uiState = MutableStateFlow(CalibrationUiState())
    val uiState: StateFlow<CalibrationUiState> = _uiState.asStateFlow()

    private var samplingJob: Job? = null
    private var readyCheckJob: Job? = null

    fun startCalibration() {
        engine = CalibrationEngine()
        startPhase(CalibrationPhase.Center)
    }

    fun advancePhase() {
        when (_uiState.value.phase) {
            CalibrationPhase.Center -> {
                engine.finalizeCenters()
                startPhase(CalibrationPhase.RangeLeft)
            }
            CalibrationPhase.RangeLeft -> startPhase(CalibrationPhase.RangeRight)
            CalibrationPhase.RangeRight -> startPhase(CalibrationPhase.DeadzoneLeft)
            CalibrationPhase.DeadzoneLeft -> startPhase(CalibrationPhase.DeadzoneRight)
            CalibrationPhase.DeadzoneRight -> {
                stopJobs()
                val result = engine.buildResult()
                _uiState.update { it.copy(phase = CalibrationPhase.Test, calibrationData = result) }
                startTestSampling()
            }
            else -> {}
        }
    }

    fun acceptCalibration() {
        val data = _uiState.value.calibrationData ?: return
        CalibrationPersistence.save(getApplication(), data)
    }

    fun retryCalibration() {
        stopJobs()
        engine = CalibrationEngine()
        _uiState.value = CalibrationUiState(phase = CalibrationPhase.Intro)
    }

    fun cancel() {
        stopJobs()
    }

    private fun startPhase(phase: CalibrationPhase) {
        stopJobs()

        val minDuration =
            when (phase) {
                CalibrationPhase.Center -> JoystickConstants.PHASE_CENTER_MIN_DURATION_MS
                CalibrationPhase.RangeLeft,
                CalibrationPhase.RangeRight -> JoystickConstants.PHASE_RANGE_MIN_DURATION_MS
                CalibrationPhase.DeadzoneLeft,
                CalibrationPhase.DeadzoneRight -> JoystickConstants.PHASE_DEADZONE_MIN_DURATION_MS
                else -> 0L
            }

        _uiState.update { it.copy(phase = phase, readyToAdvance = false) }

        if (minDuration > 0) {
            startSampling(phase)
            startReadyCheck(phase, minDuration)
        }
    }

    private fun startReadyCheck(phase: CalibrationPhase, minDurationMs: Long) {
        readyCheckJob =
            viewModelScope.launch {
                delay(minDurationMs) // let the user read the instructions first

                while (true) {
                    val ready =
                        when (phase) {
                            CalibrationPhase.Center -> engine.isCenterReady()
                            CalibrationPhase.RangeLeft -> engine.isRangeLeftReady()
                            CalibrationPhase.RangeRight -> engine.isRangeRightReady()
                            CalibrationPhase.DeadzoneLeft -> engine.isDeadzoneLeftReady()
                            CalibrationPhase.DeadzoneRight -> engine.isDeadzoneRightReady()
                            else -> false
                        }
                    if (ready) {
                        _uiState.update { it.copy(readyToAdvance = true) }
                        break
                    }
                    delay(200L)
                }
            }
    }

    private fun startSampling(phase: CalibrationPhase) {
        samplingJob =
            viewModelScope.launch {
                sampler.samples().collect { sample ->
                    _uiState.update { it.copy(currentSample = sample) }
                    when (phase) {
                        CalibrationPhase.Center -> engine.addCenterSample(sample)
                        CalibrationPhase.RangeLeft -> engine.addRangeLeftSample(sample)
                        CalibrationPhase.RangeRight -> engine.addRangeRightSample(sample)
                        CalibrationPhase.DeadzoneLeft -> engine.addDeadzoneLeftSample(sample)
                        CalibrationPhase.DeadzoneRight -> engine.addDeadzoneRightSample(sample)
                        else -> {}
                    }
                }
            }
    }

    private fun startTestSampling() {
        val data = _uiState.value.calibrationData ?: return
        samplingJob =
            viewModelScope.launch {
                sampler.samples().collect { sample ->
                    _uiState.update {
                        it.copy(
                            currentSample = sample,
                            mappedLeftX = JoystickMapping.mapAxis(sample.leftX, data.leftX),
                            mappedLeftY = JoystickMapping.mapAxis(sample.leftY, data.leftY),
                            mappedRightX = JoystickMapping.mapAxis(sample.rightX, data.rightX),
                            mappedRightY = JoystickMapping.mapAxis(sample.rightY, data.rightY),
                            rawLeftX = JoystickMapping.normalizeAxis(sample.leftX),
                            rawLeftY = JoystickMapping.normalizeAxis(sample.leftY),
                            rawRightX = JoystickMapping.normalizeAxis(sample.rightX),
                            rawRightY = JoystickMapping.normalizeAxis(sample.rightY),
                        )
                    }
                }
            }
    }

    private fun stopJobs() {
        samplingJob?.cancel()
        readyCheckJob?.cancel()
        samplingJob = null
        readyCheckJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopJobs()
    }
}
