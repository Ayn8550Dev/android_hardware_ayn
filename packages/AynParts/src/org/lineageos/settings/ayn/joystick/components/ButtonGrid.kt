/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.joystick.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ButtonGrid(
    buttons: List<Pair<String, () -> Unit>>,
    modifier: Modifier = Modifier,
    outlined: Set<Int> = emptySet(),
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        modifier = modifier.fillMaxWidth(),
    ) {
        buttons.forEachIndexed { index, (label, onClick) ->
            if (index in outlined) {
                OutlinedButton(onClick = onClick) { Text(label) }
            } else {
                Button(onClick = onClick) { Text(label) }
            }
        }
    }
}
