/*
 * SPDX-FileCopyrightText: The LineageOS Project
 * SPDX-License-Identifier: Apache-2.0
 */

package org.lineageos.settings.ayn.led

import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import com.android.settingslib.collapsingtoolbar.CollapsingToolbarBaseActivity
import kotlin.math.roundToInt
import org.lineageos.settings.ayn.R
import org.lineageos.settings.ayn.ui.AynComposeTheme

class LedSettingsActivity : CollapsingToolbarBaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    com.android.settingslib.collapsingtoolbar.R.id.content_frame,
                    LedSettingsFragment(),
                )
                .commit()
        }
    }
}

class LedSettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AynComposeTheme {
                    LedSettingsScreen()
                }
            }
        }
    }
}

@Composable
private fun LedSettingsScreen() {
    val context = LocalContext.current
    var state by remember { mutableStateOf(LedPersistence.load(context)) }
    var error by remember { mutableStateOf<LedController.Error?>(null) }

    fun updateState(newState: LedState) {
        val clamped = newState.clamped()
        state = clamped
        LedPersistence.save(context, clamped)
        error = LedController.apply(clamped)
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MasterLightingCard(
                enabled = state.enabled,
                onEnabledChanged = { enabled ->
                    val restoredBrightness = if (enabled) {
                        state.lastNonZeroBrightness
                    } else {
                        state.brightness
                    }
                    updateState(
                        state.copy(
                            enabled = enabled,
                            brightness = restoredBrightness.coerceIn(1, 255),
                            lastNonZeroBrightness = if (state.brightness > 0) {
                                state.brightness
                            } else {
                                state.lastNonZeroBrightness
                            },
                        ),
                    )
                },
            )

            Column(
                modifier = Modifier.alpha(if (state.enabled) 1f else 0.45f),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                TargetSelector(
                    selectedTarget = state.target,
                    enabled = state.enabled,
                    onTargetSelected = { updateState(state.copy(target = it)) },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = stringResource(R.string.led_strip_lights_enabled),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Switch(
                        checked = state.stripLightsEnabled,
                        enabled = state.enabled,
                        onCheckedChange = { stripLightsEnabled ->
                            updateState(state.copy(stripLightsEnabled = stripLightsEnabled))
                        },
                    )
                }

                ColorControls(
                    color = state.color,
                    enabled = state.enabled,
                    onColorChanged = { updateState(state.copy(color = it)) },
                )

                BrightnessControl(
                    brightness = state.brightness,
                    enabled = state.enabled,
                    onBrightnessChanged = { brightness ->
                        updateState(
                            state.copy(
                                brightness = brightness,
                                lastNonZeroBrightness = if (brightness > 0) {
                                    brightness
                                } else {
                                    state.lastNonZeroBrightness
                                },
                                enabled = brightness > 0,
                            ),
                        )
                    },
                )
            }

            error?.let {
                Text(
                    text = stringResource(it.messageRes),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun MasterLightingCard(
    enabled: Boolean,
    onEnabledChanged: (Boolean) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.led_enabled),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = stringResource(R.string.led_enabled_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChanged,
            )
        }
    }
}

@Composable
private fun TargetSelector(
    selectedTarget: LedTarget,
    enabled: Boolean,
    onTargetSelected: (LedTarget) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.led_target_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            LedTarget.values().forEach { target ->
                val modifier = Modifier.weight(1f)
                if (target == selectedTarget) {
                    Button(
                        modifier = modifier,
                        enabled = enabled,
                        onClick = { onTargetSelected(target) },
                    ) {
                        Text(stringResource(target.labelRes))
                    }
                } else {
                    OutlinedButton(
                        modifier = modifier,
                        enabled = enabled,
                        onClick = { onTargetSelected(target) },
                    ) {
                        Text(stringResource(target.labelRes))
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorControls(
    color: RgbColor,
    enabled: Boolean,
    onColorChanged: (RgbColor) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color(
                            red = color.red,
                            green = color.green,
                            blue = color.blue,
                        ),
                        CircleShape,
                    ),
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.led_color_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
            )
        }

        HueSaturationPicker(
            color = color,
            enabled = enabled,
            onColorChanged = onColorChanged,
        )
    }
}

@Composable
private fun HueSaturationPicker(
    color: RgbColor,
    enabled: Boolean,
    onColorChanged: (RgbColor) -> Unit,
) {
    val hsv = remember(color) {
        FloatArray(3).also {
            AndroidColor.colorToHSV(AndroidColor.rgb(color.red, color.green, color.blue), it)
        }
    }
    var size by remember { mutableStateOf(IntSize.Zero) }

    fun updateFromPosition(position: Offset) {
        if (size.width <= 0 || size.height <= 0) return

        val hue = (position.x.coerceIn(0f, size.width.toFloat()) / size.width) * 360f
        val saturation = 1f - (position.y.coerceIn(0f, size.height.toFloat()) / size.height)
        val selectedColor = AndroidColor.HSVToColor(floatArrayOf(hue, saturation, 1f))
        onColorChanged(
            RgbColor(
                red = AndroidColor.red(selectedColor),
                green = AndroidColor.green(selectedColor),
                blue = AndroidColor.blue(selectedColor),
            ),
        )
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(24.dp))
            .onSizeChanged { size = it }
            .pointerInput(enabled) {
                if (enabled) {
                    detectTapGestures { updateFromPosition(it) }
                }
            }
            .pointerInput(enabled) {
                if (enabled) {
                    detectDragGestures { change, _ -> updateFromPosition(change.position) }
                }
            },
    ) {
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Red,
                    Color.Yellow,
                    Color.Green,
                    Color.Cyan,
                    Color.Blue,
                    Color.Magenta,
                    Color.Red,
                ),
            ),
        )
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color.White),
            ),
        )

        val indicator = Offset(
            x = (hsv[0] / 360f) * this.size.width,
            y = (1f - hsv[1]) * this.size.height,
        )
        drawCircle(
            color = Color.White,
            radius = 12.dp.toPx(),
            center = indicator,
            style = Stroke(width = 3.dp.toPx()),
        )
        drawCircle(
            color = Color.Black,
            radius = 14.dp.toPx(),
            center = indicator,
            style = Stroke(width = 1.dp.toPx()),
        )
    }
}

@Composable
private fun BrightnessControl(
    brightness: Int,
    enabled: Boolean,
    onBrightnessChanged: (Int) -> Unit,
) {
    val percent = brightnessToPercent(brightness)

    Column {
        Text(
            text = stringResource(R.string.led_brightness),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Slider(
                modifier = Modifier.weight(1f),
                value = percent.toFloat(),
                enabled = enabled,
                onValueChange = { onBrightnessChanged(percentToBrightness(it)) },
                valueRange = 0f..100f,
            )
            Text(
                modifier = Modifier.width(48.dp),
                text = stringResource(R.string.led_percent_value, percent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.End,
            )
        }
    }
}

private fun brightnessToPercent(brightness: Int): Int {
    return ((brightness.coerceIn(0, 255) / 255f) * 100f).roundToInt()
}

private fun percentToBrightness(percent: Float): Int {
    return ((percent.coerceIn(0f, 100f) / 100f) * 255f).roundToInt()
}

private val LedController.Error.messageRes: Int
    get() = when (this) {
        LedController.Error.MISSING_NODE -> R.string.led_error_missing
        LedController.Error.READ_FAILED -> R.string.led_error_read
        LedController.Error.WRITE_FAILED -> R.string.led_error_write
    }
