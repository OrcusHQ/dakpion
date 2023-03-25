package com.orcuspay.dakpion.presentation.composables

import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.orcuspay.dakpion.presentation.theme.interFontFamily


@Composable
fun XTextField(
    modifier: Modifier = Modifier,
    title: String? = null,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    textStyle: TextStyle,
    singleLine: Boolean = true,
    placeholder: String = "",
    placeholderColor: Color = Color.Gray,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    maxChar: Int? = null,
    maxLines: Int = Int.MAX_VALUE,
    readOnly: Boolean = false,
    borderColor: Color = Color.Gray,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val currentBorderColor = if (isFocused)
        Color(0xFF302476)
    else borderColor
    val currentBorderWidth = if (isFocused) 3.dp else 1.dp

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
        modifier = modifier
    ) {
        if (title != null) {
            Text(
                text = title,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                fontFamily = interFontFamily,
                color = Color.Black,
            )
            Gap(height = 8.dp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .border(
                    width = currentBorderWidth,
                    color = currentBorderColor,
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(
                    start = 16.dp,
                    end = 16.dp
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterStart
            ) {
                BasicTextField(
                    interactionSource = interactionSource,
                    value = value,
                    readOnly = readOnly,
                    onValueChange = {
                        var textFieldValue: TextFieldValue = it
                        if (maxChar != null) {
                            textFieldValue = it.ofMaxLength(maxChar)
                        }

                        onValueChange(textFieldValue)
                    },
                    textStyle = textStyle.copy(textAlign = TextAlign.Start),
                    singleLine = singleLine,
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    visualTransformation = visualTransformation,
                    modifier = Modifier
                        .fillMaxWidth(),
                    maxLines = maxLines,
                )
                if (value.text.isEmpty())
                    Text(
                        text = placeholder,
                        color = placeholderColor,
                        fontSize = textStyle.fontSize,
                        fontWeight = FontWeight(400),
                    )
            }

        }
    }
}

fun TextFieldValue.ofMaxLength(maxLength: Int): TextFieldValue {
    val overLength = text.length - maxLength
    return if (overLength > 0) {
        val headIndex = selection.end - overLength
        val trailIndex = selection.end
        // Under normal conditions, headIndex >= 0
        if (headIndex >= 0) {
            copy(
                text = text.substring(0, headIndex) + text.substring(trailIndex, text.length),
                selection = TextRange(headIndex)
            )
        } else {
            // exceptional
            copy(text.take(maxLength), selection = TextRange(maxLength))
        }
    } else {
        this
    }
}