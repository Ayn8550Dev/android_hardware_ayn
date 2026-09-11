/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.utils

import android.content.Context
import android.content.Intent
import android.hardware.input.InputManager
import android.os.Bundle

object RemappingUtils {
    // :045e:028e:uniqueId:moorechip -> sha1sum
    private const val CONTROLLER_DESCRIPTOR = "90dfbbe0bbbab74642c53cae4ae0aa640d40ec21"

    fun launchGamepadRemapping(context: Context) {
        val inputManager = context.getSystemService(InputManager::class.java) ?: return
        val device = inputManager.getInputDeviceByDescriptor(CONTROLLER_DESCRIPTOR) ?: return
        val args = Bundle().apply { putParcelable("input_device_identifier", device.identifier) }

        val intent =
            Intent().apply {
                setClassName("com.android.settings", "com.android.settings.SubSettings")
                putExtra(
                    ":settings:show_fragment",
                    "com.android.settings.input.gamecontroller.GameControllerFragment",
                )
                putExtra(":settings:show_fragment_args", args)
                putExtra(":settings:show_fragment_title", device.name)
            }

        context.startActivity(intent)
    }
}
