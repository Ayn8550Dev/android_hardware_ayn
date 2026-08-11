/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

data class AxisCalibration(val center: Int, val min: Int, val max: Int, val deadzone: Int)

data class CalibrationData(
    val leftX: AxisCalibration,
    val leftY: AxisCalibration,
    val rightX: AxisCalibration,
    val rightY: AxisCalibration,
) {
    /** Serializes to the kernel's 20-field sysfs format. */
    fun toSysfsString(): String =
        listOf(
                leftX.min,
                leftX.max,
                leftX.center,
                leftX.deadzone,
                leftY.min,
                leftY.max,
                leftY.center,
                leftY.deadzone,
                rightX.min,
                rightX.max,
                rightX.center,
                rightX.deadzone,
                rightY.min,
                rightY.max,
                rightY.center,
                rightY.deadzone,
                JoystickConstants.DEFAULT_HAT_MIN,
                JoystickConstants.DEFAULT_HAT_MAX,
                JoystickConstants.DEFAULT_HAT_MIN,
                JoystickConstants.DEFAULT_HAT_MAX,
            )
            .joinToString(":")

    companion object {
        private const val EXPECTED_VALUES = 20

        /** Parses the kernel's 20-field sysfs format. Returns null on bad input. */
        fun fromSysfsString(s: String): CalibrationData? {
            val parts = s.trim().split(":")
            if (parts.size != EXPECTED_VALUES) return null
            val ints = parts.mapNotNull { it.trim().toIntOrNull() }
            if (ints.size != EXPECTED_VALUES) return null

            return CalibrationData(
                leftX =
                    AxisCalibration(
                        min = ints[0],
                        max = ints[1],
                        center = ints[2],
                        deadzone = ints[3],
                    ),
                leftY =
                    AxisCalibration(
                        min = ints[4],
                        max = ints[5],
                        center = ints[6],
                        deadzone = ints[7],
                    ),
                rightX =
                    AxisCalibration(
                        min = ints[8],
                        max = ints[9],
                        center = ints[10],
                        deadzone = ints[11],
                    ),
                rightY =
                    AxisCalibration(
                        min = ints[12],
                        max = ints[13],
                        center = ints[14],
                        deadzone = ints[15],
                    ),
            )
        }
    }
}
