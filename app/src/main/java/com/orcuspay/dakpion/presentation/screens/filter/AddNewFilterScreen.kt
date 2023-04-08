package com.orcuspay.dakpion.presentation.screens.filter

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.composables.XButton
import com.orcuspay.dakpion.presentation.composables.XTextField
import com.orcuspay.dakpion.presentation.destinations.AllDoneScreenDestination
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun AddNewFilterScreen(
    navigator: DestinationsNavigator,
    viewModel: AddNewFilterViewModel = hiltViewModel()
) {

    val (sender, setSender) = remember {
        mutableStateOf(TextFieldValue())
    }

    val (value, setValue) = remember {
        mutableStateOf(TextFieldValue())
    }

    val buttonEnabled = value.text.isNotBlank()

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopBar(showBackButton = true) {
                navigator.navigateUp()
            }
        },
    ) { pv ->

        Column(
            modifier = Modifier
                .padding(pv)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top,
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                Gap(height = 30.dp)
                XTextField(
                    value = sender,
                    onValueChange = setSender,
                    textStyle = TextStyle(
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = Color.Black,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Sender",
                    placeholder = "bKash",
                    borderColor = Color(0xFFC0C8D2),
                    placeholderColor = Color(0xFF87909F),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Gap(height = 8.dp)

                Text(
                    text = "If empty, this filter applies to all messages from all senders.",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF545969),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Gap(height = 24.dp)

                XTextField(
                    value = value,
                    onValueChange = setValue,
                    textStyle = TextStyle(
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = Color.Black,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Filter",
                    placeholder = "Enter your filter here",
                    borderColor = Color(0xFFC0C8D2),
                    placeholderColor = Color(0xFF87909F),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
                Gap(height = 8.dp)

                Text(
                    text = "Match exact string or regular expression by wrapping it with \"/\" like /regex/.",
                    fontFamily = interFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = Color(0xFF545969),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                Gap(height = 16.dp)
            }
            Gap(height = 16.dp)
            XButton(
                text = "Add filter",
                enabled = buttonEnabled,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                viewModel.createFilter(
                    sender = sender.text,
                    value = value.text,
                ) {
                    navigator.navigateUp()
                }
            }
        }
    }
}