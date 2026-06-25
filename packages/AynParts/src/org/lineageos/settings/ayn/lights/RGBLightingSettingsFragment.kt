/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.lights

import android.os.Bundle
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import org.lineageos.settings.ayn.R

class RGBLightingSettingsFragment : SettingsBasePreferenceFragment() {

    private lateinit var controller: RGBLightingController

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.rgb_lighting_panel, rootKey)
        controller = RGBLightingController(preferenceScreen)
    }
}
