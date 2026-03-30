package com.orcuspay.dakpion.presentation.screens.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.orcuspay.dakpion.presentation.composables.*
import com.orcuspay.dakpion.presentation.screens.destinations.AllDoneScreenDestination
import com.orcuspay.dakpion.presentation.theme.ErrorBg
import com.orcuspay.dakpion.presentation.theme.ErrorText
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun AddNewBusinessScreen(
    navigator: DestinationsNavigator,
    viewModel: AddNewBusinessViewModel = hiltViewModel()
) {

    val (accessKey, setAccessKey) = remember {
        mutableStateOf(TextFieldValue())
    }

    val (secretKey, setSecretKey) = remember {
        mutableStateOf(TextFieldValue())
    }


    val state = viewModel.state

    val buttonEnabled = !state.loading && accessKey.text.isNotBlank() && secretKey.text.isNotBlank()

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

                if (state.error != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ErrorBg),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = state.error,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            fontFamily = interFontFamily,
                            color = ErrorText,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                    Gap(height = 24.dp)
                }


                XTextField(
                    value = accessKey,
                    onValueChange = setAccessKey,
                    textStyle = TextStyle(
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = MaterialTheme.colors.onSurface,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Access key",
                    placeholder = "ak_prod_",
                    borderColor = Color(0xFFC0C8D2),
                    placeholderColor = Color(0xFF87909F),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Next
                    )
                )

                Gap(height = 24.dp)

                XTextField(
                    value = secretKey,
                    onValueChange = setSecretKey,
                    textStyle = TextStyle(
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 17.sp,
                        color = MaterialTheme.colors.onSurface,
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp),
                    title = "Secret key",
                    placeholder = "sk_prod_",
                    borderColor = Color(0xFFC0C8D2),
                    placeholderColor = Color(0xFF87909F),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
                Gap(height = 16.dp)
            }
            Gap(height = 16.dp)
            XButton(
                text = "Add business",
                enabled = buttonEnabled,
                loading = state.loading,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                viewModel.verify(
                    accessKey = accessKey.text,
                    secretKey = secretKey.text,
                ) {
                    navigator.navigate(AllDoneScreenDestination)
                }
            }
        }
    }
}