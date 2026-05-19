/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.led

import android.content.Context
import androidx.preference.PreferenceManager

object LedPersistence {
    private const val KEY_CONFIGURED = "led_configured"
    private const val KEY_TARGET = "led_target"
    private const val KEY_RED = "led_red"
    private const val KEY_GREEN = "led_green"
    private const val KEY_BLUE = "led_blue"
    private const val KEY_BRIGHTNESS = "led_brightness"
    private const val KEY_LAST_NON_ZERO_BRIGHTNESS = "led_last_non_zero_brightness"
    private const val KEY_ENABLED = "led_enabled"
    private const val KEY_STRIP_LIGHTS_ENABLED = "led_strip_lights_enabled"

    fun load(context: Context): LedState {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return LedState(
            target = LedTarget.fromName(prefs.getString(KEY_TARGET, LedTarget.ALL.name)),
            color = RgbColor(
                red = prefs.getInt(KEY_RED, 0),
                green = prefs.getInt(KEY_GREEN, 255),
                blue = prefs.getInt(KEY_BLUE, 255),
            ),
            brightness = prefs.getInt(KEY_BRIGHTNESS, 255),
            lastNonZeroBrightness = prefs.getInt(KEY_LAST_NON_ZERO_BRIGHTNESS, 255),
            enabled = prefs.getBoolean(KEY_ENABLED, true),
            stripLightsEnabled = prefs.getBoolean(KEY_STRIP_LIGHTS_ENABLED, true),
        ).clamped()
    }

    fun save(context: Context, state: LedState) {
        val clamped = state.clamped()
        PreferenceManager.getDefaultSharedPreferences(context).edit()
            .putBoolean(KEY_CONFIGURED, true)
            .putString(KEY_TARGET, clamped.target.name)
            .putInt(KEY_RED, clamped.color.red)
            .putInt(KEY_GREEN, clamped.color.green)
            .putInt(KEY_BLUE, clamped.color.blue)
            .putInt(KEY_BRIGHTNESS, clamped.brightness)
            .putInt(KEY_LAST_NON_ZERO_BRIGHTNESS, clamped.lastNonZeroBrightness)
            .putBoolean(KEY_ENABLED, clamped.enabled)
            .putBoolean(KEY_STRIP_LIGHTS_ENABLED, clamped.stripLightsEnabled)
            .apply()
    }

    fun restoreOnBoot(context: Context) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        if (!prefs.getBoolean(KEY_CONFIGURED, false)) return

        LedController.apply(load(context))
    }
}
