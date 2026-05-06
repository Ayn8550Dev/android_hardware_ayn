/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

object JoystickConstants {
    const val SYSFS_RAW_PATH = "/sys/class/moorechip-joystick/joystick/raw"
    const val SYSFS_CALIBRATION_PATH = "/sys/class/moorechip-joystick/joystick/calibration"

    // Raw node: keys(hex):lx:ly:rx:ry:hat2y:hat2x
    // Sticks are signed ints around 0; hat2y/hat2x are the triggers.

    const val POLL_INTERVAL_MS = 16L // ~60 Hz

    // Kernel defaults for display normalization
    const val DEFAULT_STICK_MIN = -1350
    const val DEFAULT_STICK_MAX = 1350
    const val DEFAULT_HAT_MIN = 0
    const val DEFAULT_HAT_MAX = 1550

    // How long each step waits before even checking readiness
    const val PHASE_CENTER_MIN_DURATION_MS = 2000L
    const val PHASE_RANGE_MIN_DURATION_MS = 3000L
    const val PHASE_DEADZONE_MIN_DURATION_MS = 2000L

    const val CENTER_MAX_VARIANCE = 100
    const val RANGE_MIN_DIRECTIONAL = 600 // per direction from center

    const val DEADZONE_NOISE_MULTIPLIER = 1.5f
    const val MIN_DEADZONE = 3
    const val MAX_DEADZONE = 200

    // Sysfs format (20 fields):
    // Per stick axis: min:max:center:deadzone (x4)
    // Then hats: min:max:min:max

    const val PREF_KEY_CALIBRATION = "joystick_calibration"
}
