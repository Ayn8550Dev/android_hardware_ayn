/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import android.content.Context
import androidx.preference.PreferenceManager
import org.lineageos.settings.ayn.utils.NodeUtils

object CalibrationPersistence {
    fun save(context: Context, data: CalibrationData): Boolean {
        val str = data.toSysfsString()
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(JoystickConstants.PREF_KEY_CALIBRATION, str).apply()
        return NodeUtils.write(JoystickConstants.SYSFS_CALIBRATION_PATH, str)
    }

    fun load(context: Context): CalibrationData? {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val str = prefs.getString(JoystickConstants.PREF_KEY_CALIBRATION, null) ?: return null
        return CalibrationData.fromSysfsString(str)
    }

    fun restoreOnBoot(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        val calStr = prefs.getString(JoystickConstants.PREF_KEY_CALIBRATION, null)

        if (calStr != null && CalibrationData.fromSysfsString(calStr) != null) {
            NodeUtils.write(JoystickConstants.SYSFS_CALIBRATION_PATH, calStr)
        }
    }
}
