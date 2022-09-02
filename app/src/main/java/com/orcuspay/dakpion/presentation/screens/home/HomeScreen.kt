package com.orcuspay.dakpion.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import com.orcuspay.dakpion.domain.model.Credential
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph

@OptIn(ExperimentalPermissionsApi::class)
@RootNavGraph(start = true) // sets this as the start destination of the default nav graph
@Destination
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {

    val checkedState = remember { mutableStateOf(true) }

    Column(verticalArrangement = Arrangement.Top, horizontalAlignment = Alignment.Start) {
        Text(text = "Hello ${viewModel.text}!")
        val cameraPermissionState = rememberPermissionState(
            android.Manifest.permission.READ_SMS
        )

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

        CredentialView(
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

@Composable
fun CredentialView(
    credential: Credential,
    checked: Boolean,
    onCheckedChange: (value: Boolean) -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = 8.dp,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Top
            ) {
                Text(text = credential.businessName)
                Spacer(modifier = Modifier.height(10.dp))
                Text(text = credential.mode)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }

    }
}