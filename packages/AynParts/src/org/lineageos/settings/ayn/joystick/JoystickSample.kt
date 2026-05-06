/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

data class JoystickSample(
    val leftX: Int,
    val leftY: Int,
    val rightX: Int,
    val rightY: Int,
    val leftTrigger: Int,
    val rightTrigger: Int,
) {
    companion object {
        /**
         * Parses the raw sysfs node output.
         * Format: keys:lx:ly:rx:ry:hat2y:hat2x
         */
        fun fromRawString(raw: String): JoystickSample? {
            val parts = raw.trim().split(":")
            if (parts.size != 7) return null
            val lx = parts[1].toIntOrNull() ?: return null
            val ly = parts[2].toIntOrNull() ?: return null
            val rx = parts[3].toIntOrNull() ?: return null
            val ry = parts[4].toIntOrNull() ?: return null
            val lt = parts[5].toIntOrNull() ?: return null
            val rt = parts[6].toIntOrNull() ?: return null
            return JoystickSample(lx, ly, rx, ry, lt, rt)
        }
    }
}
