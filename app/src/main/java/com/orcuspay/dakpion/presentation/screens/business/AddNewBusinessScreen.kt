package com.orcuspay.dakpion.presentation.screens.business

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.orcuspay.dakpion.presentation.destinations.AllDoneScreenDestination
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

    var isTestMode by remember {
        mutableStateOf(true)
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
            Gap(height = 30.dp)

            if (state.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFE7F2)),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = state.error,
                        fontWeight = FontWeight.Medium,
                        fontSize = 18.sp,
                        fontFamily = interFontFamily,
                        color = Color(0xFF890D37),
                        modifier = Modifier.padding(16.dp)
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
                    color = Color.Black,
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
                    color = Color.Black,
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

            Gap(height = 30.dp)

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Test mode",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp,
                    fontFamily = interFontFamily,
                    color = Color.Black,
                )

                XSwitch(
                    value = isTestMode,
                    onValueChange = {
                        isTestMode = it
                    },
                )
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.BottomCenter) {
                XButton(
                    text = "Add business",
                    enabled = buttonEnabled,
                    loading = state.loading,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                ) {
                    viewModel.verify(
                        accessKey = accessKey.text,
                        secretKey = secretKey.text,
                        isTestMode = isTestMode,
                    ) {
                        navigator.navigate(AllDoneScreenDestination)
                    }
                }
            }
        }
    }
}