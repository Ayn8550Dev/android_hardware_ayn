/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.lights

import android.content.Context
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceManager
import androidx.preference.PreferenceScreen
import com.android.settingslib.widget.MainSwitchPreference
import org.lineageos.settings.ayn.R
import org.lineageos.settings.ayn.preferences.RGBPreference
import org.lineageos.settings.ayn.utils.LightUtils

class RGBLightingController(private val preferenceScreen: PreferenceScreen) :
    Preference.OnPreferenceChangeListener {

    private val context: Context = preferenceScreen.context
    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

    private val keys = context.resources.getStringArray(R.array.rgb_preference_keys)
    private val nodes = context.resources.getStringArray(R.array.rgb_preference_nodes)
    private val defaults = context.resources.getStringArray(R.array.rgb_preference_defaults)

    private val modeCategory = preferenceScreen.findPreference<PreferenceCategory>("mode_category")
    private val leftCategory = preferenceScreen.findPreference<PreferenceCategory>("left_category")
    private val rightCategory =
        preferenceScreen.findPreference<PreferenceCategory>("right_category")

    init {
        setupListeners()
        updatePreferencesVisibility()
    }

    private fun setupListeners() {
        preferenceScreen
            .findPreference<MainSwitchPreference>("rgb_lighting_enable")
            ?.onPreferenceChangeListener = this
        preferenceScreen.findPreference<ListPreference>("rgb_mode")?.onPreferenceChangeListener =
            this
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        when (preference.key) {
            "rgb_lighting_enable" -> {
                val enabled = newValue as Boolean
                sharedPrefs.edit().putBoolean("rgb_lighting_enable", enabled).apply()
                if (enabled) restoreActiveMode(context) else LightUtils.turnOffAll(context)
                return true
            }
            "rgb_mode" -> {
                sharedPrefs.edit().putString("rgb_mode", newValue as String).apply()
                updatePreferencesVisibility()
                restoreActiveMode(context)
                return true
            }
        }
        return false
    }

    fun updatePreferencesVisibility() {
        val mode = sharedPrefs.getString("rgb_mode", "global") ?: "global"
        val listPref = preferenceScreen.findPreference<ListPreference>("rgb_mode")

        modeCategory?.apply {
            removeAll()
            listPref?.let { addPreference(it) }
        }

        leftCategory?.removeAll()
        rightCategory?.removeAll()

        when (mode) {
            "global" -> {
                toggleCategoryVisibility(leftCategory, false)
                toggleCategoryVisibility(rightCategory, false)
                modeCategory?.addPreference(
                    createRGBPreference("global_lighting", R.string.lights_rgb_mode_global)
                )
            }
            "per_side" -> {
                toggleCategoryVisibility(leftCategory, false)
                toggleCategoryVisibility(rightCategory, false)
                modeCategory?.addPreference(
                    createRGBPreference("left_side", R.string.lights_rgb_left_category_title)
                )
                modeCategory?.addPreference(
                    createRGBPreference("right_side", R.string.lights_rgb_right_category_title)
                )
            }
            "per_led" -> {
                toggleCategoryVisibility(leftCategory, true)
                toggleCategoryVisibility(rightCategory, true)

                if (LightUtils.supportsStrip) {
                    leftCategory?.addPreference(
                        createRGBPreference("left_strip", R.string.lights_rgb_strip_title)
                    )
                    rightCategory?.addPreference(
                        createRGBPreference("right_strip", R.string.lights_rgb_strip_title)
                    )
                    leftCategory?.addPreference(
                        createRGBPreference("left_stick", R.string.lights_rgb_stick_title)
                    )
                    rightCategory?.addPreference(
                        createRGBPreference("right_stick", R.string.lights_rgb_stick_title)
                    )
                }
                if (LightUtils.supportsStickPerLed) {
                    for (i in 0..3) {
                        val title = context.getString(R.string.lights_rgb_stick_led_title, i + 1)
                        leftCategory?.addPreference(createRGBPreference("left_led_$i", title))
                        rightCategory?.addPreference(createRGBPreference("right_led_$i", title))
                    }
                }
            }
        }
    }

    private fun toggleCategoryVisibility(category: PreferenceCategory?, show: Boolean) {
        if (category == null) return
        val isAttached = preferenceScreen.findPreference<PreferenceCategory>(category.key!!) != null
        if (show && !isAttached) preferenceScreen.addPreference(category)
        else if (!show && isAttached) preferenceScreen.removePreference(category)
    }

    private fun createRGBPreference(key: String, title: CharSequence): RGBPreference {
        return RGBPreference(context, null).apply {
            this.key = key
            setTitle(title)
            nodeString = getNodeForKey(key) ?: ""

            val index = keys.indexOf(key)
            if (index != -1) {
                defaults.getOrNull(index)?.let { defaultVal ->
                    defaultValue = defaultVal
                    setDefaultValue(defaultVal)
                }
            }
        }
    }

    private fun createRGBPreference(key: String, titleRes: Int): RGBPreference {
        return createRGBPreference(key, context.getString(titleRes))
    }

    private fun getNodeForKey(key: String): String? = nodes.getOrNull(keys.indexOf(key))

    companion object {
        private fun applyPreference(context: Context, prefKey: String) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            val keys = context.resources.getStringArray(R.array.rgb_preference_keys)
            val nodes = context.resources.getStringArray(R.array.rgb_preference_nodes)
            val defaults = context.resources.getStringArray(R.array.rgb_preference_defaults)

            val index = keys.indexOf(prefKey)
            if (index == -1) return

            val prefValue = sharedPrefs.getString(prefKey, defaults.getOrNull(index)) ?: return
            val nodePath = nodes.getOrNull(index) ?: return

            val parts = prefValue.split(":")
            if (parts.size >= 4) {
                val kernelColorValue = "${parts[0]} ${parts[1]} ${parts[2]}"
                LightUtils.applyToHardware(nodePath, kernelColorValue, parts[3].toInt())
            }
        }

        fun restoreActiveMode(context: Context) {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            if (!sharedPrefs.getBoolean("rgb_lighting_enable", false)) return

            when (sharedPrefs.getString("rgb_mode", "global")) {
                "global" -> applyPreference(context, "global_lighting")
                "per_side" -> {
                    applyPreference(context, "left_side")
                    applyPreference(context, "right_side")
                }
                "per_led" -> {
                    if (LightUtils.supportsStrip) {
                        applyPreference(context, "left_strip")
                        applyPreference(context, "right_strip")
                        applyPreference(context, "left_stick")
                        applyPreference(context, "right_stick")
                    }
                    if (LightUtils.supportsStickPerLed) {
                        for (i in 0..3) {
                            applyPreference(context, "left_led_$i")
                            applyPreference(context, "right_led_$i")
                        }
                    }
                }
            }
        }

        fun restoreOnBoot(context: Context) = restoreActiveMode(context)
    }
}
