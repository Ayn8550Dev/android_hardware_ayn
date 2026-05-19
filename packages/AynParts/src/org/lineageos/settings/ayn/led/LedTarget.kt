/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.led

import androidx.annotation.StringRes
import org.lineageos.settings.ayn.R

enum class LedTarget(
    @param:StringRes val labelRes: Int,
    val ledNames: List<String>,
) {
    LEFT_SIDE(
        R.string.led_target_left_side,
        listOf("left:strip", "left:stick"),
    ),
    RIGHT_SIDE(
        R.string.led_target_right_side,
        listOf("right:strip", "right:stick"),
    ),
    ALL(
        R.string.led_target_all,
        listOf("left:strip", "right:strip", "left:stick", "right:stick"),
    );

    companion object {
        fun fromName(name: String?) = when (name) {
            "LEFT_STRIP", "LEFT_STICK", LEFT_SIDE.name -> LEFT_SIDE
            "RIGHT_STRIP", "RIGHT_STICK", RIGHT_SIDE.name -> RIGHT_SIDE
            else -> values().firstOrNull { it.name == name } ?: ALL
        }
    }
}
