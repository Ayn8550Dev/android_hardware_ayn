/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.preferences

import android.content.Context
import android.util.AttributeSet
import androidx.core.content.withStyledAttributes
import androidx.preference.ListPreference

import org.lineageos.settings.ayn.R
import org.lineageos.settings.ayn.utils.NodeUtils

class NodeListPreference @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.dialogPreferenceStyle,
    defStyleRes: Int = 0
) : ListPreference(context, attrs, defStyleAttr, defStyleRes) {

    private var nodePath: String? = null
    private var siblingKey: String? = null

    private val originalEntries = entries
    private val originalValues = entryValues

    init {
        context.withStyledAttributes(attrs, R.styleable.NodeListPreference, defStyleAttr, defStyleRes) {
            nodePath = getString(R.styleable.NodeListPreference_node)
            siblingKey = getString(R.styleable.NodeListPreference_sibling)
        }

        setOnPreferenceChangeListener { _, newValue ->
            val value = newValue as String

            nodePath?.let { path ->
                if (NodeUtils.exists(path)) {
                    NodeUtils.write(path, value)
                } else {
                    false
                }
            } ?: false
        }
    }

    override fun onClick() {
        siblingKey?.let { siblingKey ->
            entries = originalEntries
            entryValues = originalValues
            val siblingValue = sharedPreferences?.getString(siblingKey, null)
            val defaultValue = entryValues[0].toString()

            if (siblingValue != defaultValue) {
                val (validEntries, validValues) = entries.zip(entryValues)
                    .filter { (_, value) -> value.toString() != siblingValue }
                    .unzip()

                entries = validEntries.toTypedArray()
                entryValues = validValues.toTypedArray()
            }
        }

        super.onClick()
    }

    override fun onAttached() {
        super.onAttached()
        val path = nodePath ?: return

        if (!NodeUtils.exists(path)) {
            isVisible = false
            return
        }

        getValueFromNode()?.let { value = it }

        val nodeKey = "$key:node"
        if (sharedPreferences?.contains(nodeKey) == false) {
            sharedPreferences?.edit()?.putString(nodeKey, path)?.apply()
        }
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        value = getValueFromNode() ?: getPersistedString(defaultValue as? String)
    }

    private fun getValueFromNode(): String? {
        return nodePath?.takeIf { NodeUtils.exists(it) }?.let { path ->
            NodeUtils.read(path)?.takeIf { entryValues.contains(it) }
        }
    }
}
