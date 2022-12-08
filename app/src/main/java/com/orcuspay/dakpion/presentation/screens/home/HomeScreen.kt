package com.orcuspay.dakpion.presentation.screens.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.orcuspay.dakpion.domain.model.Credential
import com.orcuspay.dakpion.presentation.composables.CredentialCard
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.screens.HomeViewModel
import com.ramcosta.composedestinations.annotation.Destination

@OptIn(ExperimentalPermissionsApi::class)
@Destination
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {

    val checkedState = remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start) {

        TopBar(title = "Home")

        Text(text = "Hello ${viewModel.text}!")


        Button(onClick = { viewModel.testVerify() }) {
            if (viewModel.state.loading) {
                Text("Loading..")
            } else
                Text("Click me!")
        }

        Spacer(modifier = Modifier.width(10.dp))

        Button(onClick = { viewModel.testSend() }) {
            if (viewModel.state.loading) {
                Text("Loading..")
            } else
                Text("Click me!")
        }

        Spacer(modifier = Modifier.width(10.dp))

        CredentialCard(
            credential = Credential(
                id = 0,
                accessKey = "test",
                secretKey = "test",
                mode = "test",
                credentialId = "credentialId",
                businessName = "Zanvent"
            ),
            checked = checkedState.value
        ) {
            checkedState.value = it
        }
    }
}