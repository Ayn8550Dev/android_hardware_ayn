/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import org.lineageos.settings.ayn.utils.LightUtils

class AynSettingsFragment :
    SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ayn_panel, rootKey)

        if (!LightUtils.supportsRGB) {
            findPreference<PreferenceCategory>(KEY_GAMEPAD_LIGHTS_CATEGORY)?.let {
                preferenceScreen.removePreference(it)
            }
        }
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any?): Boolean {
        return true
    }

    companion object {
        private const val KEY_GAMEPAD_LIGHTS_CATEGORY = "gamepad_lights_category"
    }
}
