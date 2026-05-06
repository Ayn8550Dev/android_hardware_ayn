/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import kotlin.math.abs
import kotlin.math.roundToInt

class CalibrationEngine {
    private val centerLx = mutableListOf<Int>()
    private val centerLy = mutableListOf<Int>()
    private val centerRx = mutableListOf<Int>()
    private val centerRy = mutableListOf<Int>()

    private var minLx = Int.MAX_VALUE; private var maxLx = Int.MIN_VALUE
    private var minLy = Int.MAX_VALUE; private var maxLy = Int.MIN_VALUE
    private var minRx = Int.MAX_VALUE; private var maxRx = Int.MIN_VALUE
    private var minRy = Int.MAX_VALUE; private var maxRy = Int.MIN_VALUE

    // Only resting samples are kept here, movement is filtered out.
    private val dzRestLx = mutableListOf<Int>()
    private val dzRestLy = mutableListOf<Int>()
    private var leftWasMoving = false
    private var leftSettleCount = 0
    private val dzRestRx = mutableListOf<Int>()
    private val dzRestRy = mutableListOf<Int>()
    private var rightWasMoving = false
    private var rightSettleCount = 0

    // Rough centers from the initial resting step, used to judge range coverage.
    private var cLx = 0; private var cLy = 0
    private var cRx = 0; private var cRy = 0

    fun addCenterSample(sample: JoystickSample) {
        centerLx.add(sample.leftX)
        centerLy.add(sample.leftY)
        centerRx.add(sample.rightX)
        centerRy.add(sample.rightY)
    }

    fun isCenterReady(): Boolean {
        if (centerLx.size < STABLE_WINDOW) return false
        // Only the tail end matters; early outliers (accidental bumps) are ignored.
        return isStable(centerLx.takeLast(STABLE_WINDOW)) &&
            isStable(centerLy.takeLast(STABLE_WINDOW)) &&
            isStable(centerRx.takeLast(STABLE_WINDOW)) &&
            isStable(centerRy.takeLast(STABLE_WINDOW))
    }

    fun finalizeCenters() {
        cLx = centerLx.takeLast(STABLE_WINDOW).average().roundToInt()
        cLy = centerLy.takeLast(STABLE_WINDOW).average().roundToInt()
        cRx = centerRx.takeLast(STABLE_WINDOW).average().roundToInt()
        cRy = centerRy.takeLast(STABLE_WINDOW).average().roundToInt()
    }

    fun addRangeLeftSample(sample: JoystickSample) {
        minLx = minOf(minLx, sample.leftX); maxLx = maxOf(maxLx, sample.leftX)
        minLy = minOf(minLy, sample.leftY); maxLy = maxOf(maxLy, sample.leftY)
    }

    // All four directions from center must be reached, not just a big total span.
    fun isRangeLeftReady(): Boolean {
        val threshold = JoystickConstants.RANGE_MIN_DIRECTIONAL
        return (cLx - minLx) >= threshold &&
            (maxLx - cLx) >= threshold &&
            (cLy - minLy) >= threshold &&
            (maxLy - cLy) >= threshold
    }

    fun addRangeRightSample(sample: JoystickSample) {
        minRx = minOf(minRx, sample.rightX); maxRx = maxOf(maxRx, sample.rightX)
        minRy = minOf(minRy, sample.rightY); maxRy = maxOf(maxRy, sample.rightY)
    }

    fun isRangeRightReady(): Boolean {
        val threshold = JoystickConstants.RANGE_MIN_DIRECTIONAL
        return (cRx - minRx) >= threshold &&
            (maxRx - cRx) >= threshold &&
            (cRy - minRy) >= threshold &&
            (maxRy - cRy) >= threshold
    }

    fun addDeadzoneLeftSample(sample: JoystickSample) {
        val dist = maxOf(abs(sample.leftX - cLx), abs(sample.leftY - cLy))
        val isMoving = dist > MOVEMENT_THRESHOLD

        if (isMoving) {
            leftWasMoving = true
        } else {
            if (leftWasMoving) {
                leftSettleCount++
                leftWasMoving = false
            }
            dzRestLx.add(sample.leftX)
            dzRestLy.add(sample.leftY)
        }
    }

    // Need multiple pull-and-release cycles so we can see how much
    // the resting position varies between settles (mechanical hysteresis).
    fun isDeadzoneLeftReady(): Boolean {
        if (leftSettleCount < MIN_SETTLE_EVENTS) return false
        if (dzRestLx.size < STABLE_WINDOW) return false
        return isStable(dzRestLx.takeLast(STABLE_WINDOW)) &&
            isStable(dzRestLy.takeLast(STABLE_WINDOW))
    }

    fun addDeadzoneRightSample(sample: JoystickSample) {
        val dist = maxOf(abs(sample.rightX - cRx), abs(sample.rightY - cRy))
        val isMoving = dist > MOVEMENT_THRESHOLD

        if (isMoving) {
            rightWasMoving = true
        } else {
            if (rightWasMoving) {
                rightSettleCount++
                rightWasMoving = false
            }
            dzRestRx.add(sample.rightX)
            dzRestRy.add(sample.rightY)
        }
    }

    fun isDeadzoneRightReady(): Boolean {
        if (rightSettleCount < MIN_SETTLE_EVENTS) return false
        if (dzRestRx.size < STABLE_WINDOW) return false
        return isStable(dzRestRx.takeLast(STABLE_WINDOW)) &&
            isStable(dzRestRy.takeLast(STABLE_WINDOW))
    }

    fun buildResult(): CalibrationData {
        // The "real" center is where the stick rests after being deflected, not
        // the cold-start position. Average all resting samples across every
        // settle event to get a representative value.
        val trueCenterLx = dzRestLx.average().roundToInt()
        val trueCenterLy = dzRestLy.average().roundToInt()
        val trueCenterRx = dzRestRx.average().roundToInt()
        val trueCenterRy = dzRestRy.average().roundToInt()

        return CalibrationData(
            leftX = AxisCalibration(
                center = trueCenterLx,
                min = minLx,
                max = maxLx,
                deadzone = computeDeadzone(dzRestLx, trueCenterLx),
            ),
            leftY = AxisCalibration(
                center = trueCenterLy,
                min = minLy,
                max = maxLy,
                deadzone = computeDeadzone(dzRestLy, trueCenterLy),
            ),
            rightX = AxisCalibration(
                center = trueCenterRx,
                min = minRx,
                max = maxRx,
                deadzone = computeDeadzone(dzRestRx, trueCenterRx),
            ),
            rightY = AxisCalibration(
                center = trueCenterRy,
                min = minRy,
                max = maxRy,
                deadzone = computeDeadzone(dzRestRy, trueCenterRy),
            ),
        )
    }

    // Worst deviation from center across all settle events, with a safety margin.
    private fun computeDeadzone(restingSamples: List<Int>, center: Int): Int {
        if (restingSamples.isEmpty()) return JoystickConstants.MIN_DEADZONE
        val maxDeviation = restingSamples.maxOf { abs(it - center) }
        val dz = (maxDeviation * JoystickConstants.DEADZONE_NOISE_MULTIPLIER).roundToInt()
        return dz.coerceIn(JoystickConstants.MIN_DEADZONE, JoystickConstants.MAX_DEADZONE)
    }

    private fun isStable(samples: List<Int>): Boolean {
        if (samples.size < 2) return false
        val mean = samples.average()
        val variance = samples.sumOf { (it - mean) * (it - mean) } / samples.size
        return variance < JoystickConstants.CENTER_MAX_VARIANCE
    }

    companion object {
        const val STABLE_WINDOW = 60 // ~1 s at 60 Hz
        const val MOVEMENT_THRESHOLD = 300 // far enough from center to count as "not resting"
        const val MIN_SETTLE_EVENTS = 3
    }
}
