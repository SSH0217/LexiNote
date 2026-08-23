package com.example.vocanote.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SingleRingTimePickerState(initialHour: Int, initialMinute: Int) {
    var hour by mutableStateOf(initialHour)
    var minute by mutableStateOf(initialMinute)
}

@Composable
fun rememberSingleRingTimePickerState(
    initialHour: Int = 0,
    initialMinute: Int = 0
): SingleRingTimePickerState = remember { SingleRingTimePickerState(initialHour, initialMinute) }

/**
 * 24시간 표시에서도 안쪽/바깥쪽 두 개의 원으로 나뉘지 않고,
 * 하나의 원판에서 0~23시를 모두 선택할 수 있는 시간 선택기.
 */
@Composable
fun SingleRingTimePicker(
    state: SingleRingTimePickerState,
    modifier: Modifier = Modifier
) {
    var selectingHour by remember { mutableStateOf(true) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TimeUnitBox(
                value = state.hour,
                selected = selectingHour,
                onClick = { selectingHour = true }
            )
            Text(
                ":",
                style = MaterialTheme.typography.displayMedium,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            TimeUnitBox(
                value = state.minute,
                selected = !selectingHour,
                onClick = { selectingHour = false }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (selectingHour) {
            ClockDial(
                valueCount = 24,
                labelStep = 1,
                selectedValue = state.hour,
                onValueSelected = { state.hour = it },
                onSelectionFinished = { selectingHour = false }
            )
        } else {
            ClockDial(
                valueCount = 60,
                labelStep = 5,
                selectedValue = state.minute,
                onValueSelected = { state.minute = it }
            )
        }
    }
}

@Composable
private fun TimeUnitBox(value: Int, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(80.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            value.toString().padStart(2, '0'),
            style = MaterialTheme.typography.displayMedium,
            color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private const val DialSizeDp = 268

@Composable
private fun ClockDial(
    valueCount: Int,
    labelStep: Int,
    selectedValue: Int,
    onValueSelected: (Int) -> Unit,
    onSelectionFinished: (() -> Unit)? = null
) {
    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val primary = MaterialTheme.colorScheme.primary
    val onPrimary = MaterialTheme.colorScheme.onPrimary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val dialBackground = MaterialTheme.colorScheme.surfaceVariant

    fun angleToValue(center: Offset, position: Offset): Int {
        val dx = position.x - center.x
        val dy = position.y - center.y
        var degrees = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())) + 90.0
        if (degrees < 0) degrees += 360.0
        val step = 360.0 / valueCount
        return Math.round(degrees / step).toInt() % valueCount
    }

    Box(
        modifier = Modifier
            .size(DialSizeDp.dp)
            .pointerInput(valueCount) {
                detectTapGestures { offset ->
                    onValueSelected(angleToValue(Offset(size.width / 2f, size.height / 2f), offset))
                    onSelectionFinished?.invoke()
                }
            }
            .pointerInput(valueCount) {
                detectDragGestures(
                    onDragEnd = { onSelectionFinished?.invoke() }
                ) { change, _ ->
                    change.consume()
                    onValueSelected(angleToValue(Offset(size.width / 2f, size.height / 2f), change.position))
                }
            }
    ) {
        Canvas(modifier = Modifier.size(DialSizeDp.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            drawCircle(color = dialBackground, radius = size.minDimension / 2f, center = center)

            val labelRadius = size.minDimension / 2f - with(density) { 28.dp.toPx() }
            val step = 360.0 / valueCount

            val selectedAngleRad = Math.toRadians(selectedValue * step - 90.0)
            val knobCenter = Offset(
                x = center.x + labelRadius * cos(selectedAngleRad).toFloat(),
                y = center.y + labelRadius * sin(selectedAngleRad).toFloat()
            )
            drawLine(
                color = primary,
                start = center,
                end = knobCenter,
                strokeWidth = with(density) { 2.dp.toPx() }
            )
            drawCircle(color = primary, radius = with(density) { 4.dp.toPx() }, center = center)
            drawCircle(color = primary, radius = with(density) { 18.dp.toPx() }, center = knobCenter)

            for (i in 0 until valueCount step labelStep) {
                val angle = Math.toRadians(i * step - 90.0)
                val pos = Offset(
                    x = center.x + labelRadius * cos(angle).toFloat(),
                    y = center.y + labelRadius * sin(angle).toFloat()
                )
                val isSelected = i == selectedValue
                val label = i.toString().padStart(2, '0')
                val textLayout = textMeasurer.measure(
                    text = AnnotatedString(label),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) onPrimary else onSurfaceVariant
                    )
                )
                drawText(
                    textLayoutResult = textLayout,
                    topLeft = Offset(
                        pos.x - textLayout.size.width / 2f,
                        pos.y - textLayout.size.height / 2f
                    )
                )
            }
        }
    }
}
