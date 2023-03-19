package com.orcuspay.dakpion.presentation.composables

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.scale
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt


@Composable
fun XSwitch(
    modifier: Modifier = Modifier,
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
) {

    // this is to disable the ripple effect
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val offsetY = remember { mutableStateOf(0f) }

    val width = 50.dp
    val thumbSize = 30.dp
    val maxThumbOffset = with(LocalDensity.current) { 20.dp.toPx() }
    val offsetX = remember(value) {
//        Log.d("kraken", "calculated")
        mutableStateOf(if (value) maxThumbOffset else 0f)
    }

    val animateOffsetX = animateAlignmentAsState(targetBiasValue = offsetX.value)

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 30.dp)
            .clip(RoundedCornerShape(99.dp))
            .background(
                if (value) MaterialTheme.colors.primaryVariant else Color(0xFFEBEEF1)
            )
            .clickable(
                indication = null,
                interactionSource = interactionSource
            ) {

                if (value) {
                    offsetX.value = 0f
                } else {
                    offsetX.value = maxThumbOffset
                }
                onValueChange(!value)
            },
        contentAlignment = Alignment.Center
    ) {


        // this is to add padding at the each horizontal side
        Box(
            modifier = Modifier
                .fillMaxSize(),
        ) {

            // thumb with icon
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            animateOffsetX.value.roundToInt(),
                            offsetY.value.roundToInt()
                        )
                    }
                    .size(size = 30.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .padding(1.dp)
                    .drawColoredShadow()
                    .background(
                        color = Color.White,
                        shape = CircleShape
                    )
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                if (offsetX.value < 20.dp
                                        .div(2)
                                        .toPx()
                                ) {
                                    offsetX.value = 0f
                                } else {
                                    offsetX.value = 20.dp.toPx()
                                }

                                if (offsetX.value == 0f) {
                                    onValueChange(false)
                                } else if (offsetX.value == maxThumbOffset) {
                                    onValueChange(true)
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX.value = (offsetX.value + dragAmount.x)
                                .coerceIn(
                                    0f,
                                    20.dp.toPx()
                                )
                        }
                    },
            )
        }
    }
}

fun Modifier.drawColoredShadow(
    color: Color = Color.Black,
    alpha: Float = 0.07f,
    borderRadius: Dp = 0.dp,
    offsetX: Dp = 0.dp,
    offsetY: Dp = 0.dp,
    blurRadius: Dp = 7.dp,
    spread: Dp = 0.dp,
    enabled: Boolean = true,
) = if (enabled) {
    this.drawBehind {
        val transparentColor = color.copy(alpha = 0.0f).toArgb()
        val shadowColor = color.copy(alpha = alpha).toArgb()
        this.drawIntoCanvas {
            val paint = Paint()
            val frameworkPaint = paint.asFrameworkPaint()
            frameworkPaint.color = transparentColor
            frameworkPaint.setShadowLayer(
                blurRadius.toPx(),
                offsetX.toPx(),
                offsetY.toPx(),
                shadowColor
            )
            it.save()

            if (spread.value > 0) {
                fun calcSpreadScale(spread: Float, childSize: Float): Float {
                    return 1f + ((spread / childSize) * 2f)
                }

                it.scale(
                    calcSpreadScale(spread.toPx(), this.size.width),
                    calcSpreadScale(spread.toPx(), this.size.height),
                    this.center.x,
                    this.center.y
                )
            }

            it.drawRoundRect(
                0f,
                0f,
                this.size.width,
                this.size.height,
                borderRadius.toPx(),
                borderRadius.toPx(),
                paint
            )
            it.restore()
        }
    }
} else {
    this
}

@Composable
private fun animateAlignmentAsState(
    targetBiasValue: Float
): State<Float> {
    val bias by animateFloatAsState(targetBiasValue)
    return derivedStateOf { bias }
}