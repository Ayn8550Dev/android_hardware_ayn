/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager

import org.lineageos.settings.ayn.led.LedPersistence
import org.lineageos.settings.ayn.joystick.CalibrationPersistence
import org.lineageos.settings.ayn.utils.NodeUtils

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
        val allPrefs = sharedPrefs.all

        allPrefs.forEach { (key, value) ->
            if (key.endsWith(":node")) {
                val nodePath = value as? String
                val prefKey = key.removeSuffix(":node")
                val prefValue = allPrefs[prefKey] as? String

                if (nodePath != null && prefValue != null && NodeUtils.exists(nodePath)) {
                    NodeUtils.write(nodePath, prefValue)
                }
            }
        }

        // Restore joystick calibration
        CalibrationPersistence.restoreOnBoot(context)

        // Restore LED lighting
        LedPersistence.restoreOnBoot(context)
    }

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }
}
