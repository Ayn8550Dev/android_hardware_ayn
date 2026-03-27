/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.utils

import android.util.Log
import java.io.File

object NodeUtils {

    private const val TAG = "NodeUtils"

    fun exists(path: String): Boolean {
        val file = File(path)
        val exists = file.exists()
        if (!exists) {
            Log.w(TAG, "Node does not exist at $path")
        }
        return exists
    }

    fun read(path: String): String? {
        return try {
            File(path).readText().trim()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read node at $path", e)
            null
        }
    }

    fun write(path: String, value: String): Boolean {
        return try {
            File(path).writeText(value)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write '$value' to node at $path", e)
            false
        }
    }
}
