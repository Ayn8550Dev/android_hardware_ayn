/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.utils

import android.content.Context
import org.lineageos.settings.ayn.R

object LightUtils {

    val supportsRGB: Boolean
        get() =
            NodeUtils.exists("/sys/class/leds/left:stick") ||
                NodeUtils.exists("/sys/class/leds/left:stick:0")

    val supportsStickPerLed: Boolean
        get() = NodeUtils.exists("/sys/class/leds/left:stick:0")

    val supportsStrip: Boolean
        get() = NodeUtils.exists("/sys/class/leds/left:strip")

    fun applyToHardware(nodeString: String, kernelColorValue: String, brightness: Int) {
        nodeString
            .split(" ")
            .filter { it.isNotEmpty() }
            .forEach { node ->
                val path = "/sys/class/leds/${node.trim()}"
                if (NodeUtils.exists(path)) {
                    NodeUtils.write("$path/multi_intensity", kernelColorValue)
                    NodeUtils.write("$path/brightness", brightness.toString())
                }
            }
    }

    fun turnOffAll(context: Context) {
        val nodes = context.resources.getStringArray(R.array.rgb_preference_nodes)
        applyToHardware(nodes[0], "0 0 0", 0)
    }
}
