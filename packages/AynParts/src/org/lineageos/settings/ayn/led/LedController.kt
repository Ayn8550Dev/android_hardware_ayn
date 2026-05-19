/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.led

import org.lineageos.settings.ayn.utils.NodeUtils

object LedController {
    private const val LED_BASE_PATH = "/sys/class/leds"
    private val ALL_LED_NAMES = listOf("left:strip", "left:stick", "right:strip", "right:stick")
    private val STRIP_LED_NAMES = listOf("left:strip", "right:strip")

    enum class Error {
        MISSING_NODE,
        READ_FAILED,
        WRITE_FAILED,
    }

    fun apply(state: LedState): Error? {
        val clamped = state.clamped()

        if (!clamped.enabled) {
            ALL_LED_NAMES.forEach { ledName ->
                val error = setLedBrightness(ledName, 0)
                if (error != null) return error
            }
            return null
        }

        clamped.target.ledNames
            .filter { clamped.stripLightsEnabled || !it.isStripLed() }
            .forEach { ledName ->
                val error = setLedColor(ledName, clamped.color, clamped.brightness)
                if (error != null) return error
            }

        if (!clamped.stripLightsEnabled) {
            STRIP_LED_NAMES.forEach { ledName ->
                val error = setLedBrightness(ledName, 0)
                if (error != null) return error
            }
        }

        return null
    }

    private fun String.isStripLed() = endsWith(":strip")

    private fun setLedBrightness(ledName: String, brightness: Int): Error? {
        val brightnessPath = "$LED_BASE_PATH/$ledName/brightness"

        if (!NodeUtils.exists(brightnessPath)) {
            return Error.MISSING_NODE
        }

        if (!NodeUtils.write(brightnessPath, brightness.coerceIn(0, 255).toString())) {
            return Error.WRITE_FAILED
        }

        return null
    }

    private fun setLedColor(ledName: String, color: RgbColor, brightness: Int): Error? {
        val ledPath = "$LED_BASE_PATH/$ledName"
        val multiIndexPath = "$ledPath/multi_index"
        val multiIntensityPath = "$ledPath/multi_intensity"
        val brightnessPath = "$ledPath/brightness"

        if (!NodeUtils.exists(multiIntensityPath) || !NodeUtils.exists(brightnessPath)) {
            return Error.MISSING_NODE
        }

        val multiIndex = NodeUtils.read(multiIndexPath) ?: return Error.READ_FAILED
        val multiIntensity = orderColorForMultiIndex(color.clamped(), multiIndex)
            ?: return Error.READ_FAILED

        if (!NodeUtils.write(multiIntensityPath, multiIntensity)) {
            return Error.WRITE_FAILED
        }

        if (!NodeUtils.write(brightnessPath, brightness.coerceIn(0, 255).toString())) {
            return Error.WRITE_FAILED
        }

        return null
    }

    private fun orderColorForMultiIndex(color: RgbColor, multiIndex: String): String? {
        return multiIndex
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .map { channel ->
                when (channel) {
                    "red" -> color.red
                    "green" -> color.green
                    "blue" -> color.blue
                    else -> return null
                }
            }
            .joinToString(" ")
            .takeIf { it.isNotBlank() }
    }
}
