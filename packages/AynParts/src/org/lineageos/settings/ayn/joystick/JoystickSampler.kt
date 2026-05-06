/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import org.lineageos.settings.ayn.utils.NodeUtils

class JoystickSampler {
    fun samples(): Flow<JoystickSample> = flow {
        while (currentCoroutineContext().isActive) {
            val sample = poll()
            if (sample != null) {
                emit(sample)
            }
            delay(JoystickConstants.POLL_INTERVAL_MS)
        }
    }

    private fun poll(): JoystickSample? {
        val raw = NodeUtils.read(JoystickConstants.SYSFS_RAW_PATH) ?: return null
        return JoystickSample.fromRawString(raw)
    }
}
