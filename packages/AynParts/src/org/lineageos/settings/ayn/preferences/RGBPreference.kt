/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.preferences

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.DialogPreference
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.preference.app.R as settingslib_R
import com.google.android.material.slider.Slider
import org.lineageos.settings.ayn.R
import org.lineageos.settings.ayn.utils.LightUtils

class RGBPreference : DialogPreference {

    var nodeString: String = ""
    var defaultValue: String = "255:255:255:255"

    var selectedColor: Int = Color.WHITE
        private set

    var selectedBrightness: Int = 255
        private set

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setLayoutResource(settingslib_R.layout.preference_app)
    }

    constructor(context: Context) : this(context, null)

    override fun onAttachedToHierarchy(preferenceManager: PreferenceManager) {
        super.onAttachedToHierarchy(preferenceManager)
        loadPersistedState()
    }

    fun loadPersistedState() {
        val parts = getPersistedString(defaultValue).split(":")
        selectedColor = Color.rgb(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        selectedBrightness = parts[3].toInt()

        val size = (40 * context.resources.displayMetrics.density).toInt()
        icon =
            ShapeDrawable(OvalShape()).apply {
                intrinsicWidth = size
                intrinsicHeight = size
                paint.color = selectedColor
            }
    }

    override fun onClick() {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_rgb_sliders, null)
        val redSlider = view.findViewById<Slider>(R.id.dialog_slider_red)
        val greenSlider = view.findViewById<Slider>(R.id.dialog_slider_green)
        val blueSlider = view.findViewById<Slider>(R.id.dialog_slider_blue)
        val brightnessSlider = view.findViewById<Slider>(R.id.dialog_slider_brightness)

        val initialColor = selectedColor
        val initialBrightness = selectedBrightness

        var liveColor = selectedColor
        var liveBrightness = selectedBrightness

        redSlider.value = Color.red(liveColor).toFloat()
        greenSlider.value = Color.green(liveColor).toFloat()
        blueSlider.value = Color.blue(liveColor).toFloat()
        brightnessSlider.value = liveBrightness.toFloat()

        val getKernelColor = { color: Int ->
            "${Color.red(color)} ${Color.green(color)} ${Color.blue(color)}"
        }

        val liveApply = {
            LightUtils.applyToHardware(nodeString, getKernelColor(liveColor), liveBrightness)
        }

        val updateColor = { red: Int, green: Int, blue: Int ->
            liveColor = Color.rgb(red, green, blue)
            liveApply()
        }

        fun Slider.bind(onChange: (Float) -> Unit) {
            addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    onChange(value)
                    liveApply()
                }
            }
        }

        redSlider.bind { updateColor(it.toInt(), Color.green(liveColor), Color.blue(liveColor)) }
        greenSlider.bind { updateColor(Color.red(liveColor), it.toInt(), Color.blue(liveColor)) }
        blueSlider.bind { updateColor(Color.red(liveColor), Color.green(liveColor), it.toInt()) }
        brightnessSlider.bind { liveBrightness = it.toInt() }

        val resetToInitial = {
            LightUtils.applyToHardware(nodeString, getKernelColor(initialColor), initialBrightness)
        }

        AlertDialog.Builder(context)
            .setTitle(title)
            .setView(view)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                val stringValue =
                    "${Color.red(liveColor)}:${Color.green(liveColor)}:${Color.blue(liveColor)}:$liveBrightness"
                if (callChangeListener(stringValue)) {
                    persistString(stringValue)
                    loadPersistedState()
                    liveApply()
                }
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> resetToInitial() }
            .setOnCancelListener { resetToInitial() }
            .show()
    }
}
