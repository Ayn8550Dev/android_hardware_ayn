/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn

import android.database.Cursor
import android.database.MatrixCursor
import android.provider.SearchIndexableResource
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_CLASS_NAME
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_ICON_RESID
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_ACTION
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RANK
import android.provider.SearchIndexablesContract.COLUMN_INDEX_XML_RES_RESID
import android.provider.SearchIndexablesContract.INDEXABLES_RAW_COLUMNS
import android.provider.SearchIndexablesContract.INDEXABLES_XML_RES_COLUMNS
import android.provider.SearchIndexablesContract.NON_INDEXABLES_KEYS_COLUMNS
import android.provider.SearchIndexablesProvider
import org.lineageos.settings.ayn.utils.NodeUtils

class ConfigPanelSearchIndexablesProvider : SearchIndexablesProvider() {
    override fun onCreate(): Boolean = true

    override fun queryXmlResources(projection: Array<String?>?): Cursor {
        val cursor = MatrixCursor(INDEXABLES_XML_RES_COLUMNS)
        INDEXABLE_RES.forEach { cursor.addRow(generateResourceRef(it)) }
        return cursor
    }

    override fun queryRawData(projection: Array<String?>?): Cursor {
        return MatrixCursor(INDEXABLES_RAW_COLUMNS)
    }

    override fun queryNonIndexableKeys(projection: Array<String?>?): Cursor {
        return MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS).apply {
            NODE_PREFERENCES.forEach { (key, nodePath) ->
                if (!NodeUtils.exists("$JOYSTICK_PATH/$nodePath")) {
                    addRow(arrayOf(key))
                }
            }
        }
    }

    private fun generateResourceRef(sir: SearchIndexableResource): Array<Any?> {
        val ref = arrayOfNulls<Any>(7)
        ref[COLUMN_INDEX_XML_RES_RANK] = sir.rank
        ref[COLUMN_INDEX_XML_RES_RESID] = sir.xmlResId
        ref[COLUMN_INDEX_XML_RES_CLASS_NAME] = null
        ref[COLUMN_INDEX_XML_RES_ICON_RESID] = sir.iconResId
        ref[COLUMN_INDEX_XML_RES_INTENT_ACTION] = "com.android.settings.action.IA_SETTINGS"
        ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_PACKAGE] = "org.lineageos.settings.ayn"
        ref[COLUMN_INDEX_XML_RES_INTENT_TARGET_CLASS] = sir.className
        return ref
    }

    companion object {
        private const val TAG = "ConfigPanelSearchIndexablesProvider"

        private const val JOYSTICK_PATH = "/sys/class/moorechip-joystick/joystick"

        private val NODE_PREFERENCES =
            mapOf(
                "gamepad_layout" to "layout",
                "gamepad_trigger_mode" to "triggers",
                "gamepad_digital_trigger_threshold" to "digital_trigger_threshold",
                "gamepad_mappable_0" to "m0_function",
                "gamepad_mappable_1" to "m1_function",
                "gamepad_ignore_mask" to "ignore_mask",
            )

        private val INDEXABLE_RES =
            arrayOf<SearchIndexableResource>(
                SearchIndexableResource(1, R.xml.ayn_panel, AynSettingsActivity::class.java.name, 0)
            )
    }
}
