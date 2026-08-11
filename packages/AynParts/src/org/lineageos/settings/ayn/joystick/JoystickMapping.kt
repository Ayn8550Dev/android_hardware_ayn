/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

object JoystickMapping {
    /**
     * Applies calibration to a raw axis value, returning -1.0..1.0. Anything inside the deadzone
     * snaps to 0; outside it ramps linearly. Hardware axes run opposite to screen, so the value is
     * negated.
     */
    fun mapAxis(raw: Int, cal: AxisCalibration): Float {
        val centered = -(raw - cal.center)
        if (kotlin.math.abs(centered) <= cal.deadzone) return 0f

        return if (centered < 0) {
            val range = (cal.max - cal.center).coerceAtLeast(1)
            val adjusted = centered + cal.deadzone
            (adjusted.toFloat() / (range - cal.deadzone)).coerceIn(-1f, 0f)
        } else {
            val range = (cal.center - cal.min).coerceAtLeast(1)
            val adjusted = centered - cal.deadzone
            (adjusted.toFloat() / (range - cal.deadzone)).coerceIn(0f, 1f)
        }
    }

    /** Quick normalization using kernel defaults, for the live visualizer. */
    fun normalizeAxis(raw: Int): Float {
        return (-raw.toFloat() / JoystickConstants.DEFAULT_STICK_MAX).coerceIn(-1f, 1f)
    }

    fun normalizeTrigger(raw: Int): Float {
        val range = JoystickConstants.DEFAULT_HAT_MAX - JoystickConstants.DEFAULT_HAT_MIN
        return ((raw - JoystickConstants.DEFAULT_HAT_MIN).toFloat() / range).coerceIn(0f, 1f)
    }
}
