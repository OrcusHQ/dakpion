package com.orcuspay.dakpion.presentation.screens.home

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.presentation.composables.CredentialCard
import com.orcuspay.dakpion.presentation.composables.Gap
import com.orcuspay.dakpion.presentation.composables.TopBar
import com.orcuspay.dakpion.presentation.theme.epilogueFontFamily
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {

    val checkedState = remember { mutableStateOf(true) }
    val credentials = viewModel.credentials
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopBar(title = "Home")
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
                shape = CircleShape,
                backgroundColor = MaterialTheme.colors.primaryVariant,
                modifier = Modifier
                    .size(86.dp)
                    .offset(y = (-100).dp),
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_add),
                    contentDescription = "Add"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { pv ->
        var credentialToDelete by remember { mutableStateOf(-1) }
        var dismissing by remember {
            mutableStateOf(false)
        }
        var onDismissCallback: () -> Unit by remember {
            mutableStateOf({})
        }

        if (credentialToDelete != -1) {
            Dialog(
                onDismissRequest = {
                    credentialToDelete = -1
                    onDismissCallback()
                },
                properties = DialogProperties(
                    dismissOnClickOutside = true,
                )
            ) {
                ConfirmDialog(
                    onCancel = {
                        credentialToDelete = -1
                        onDismissCallback()
                    }
                ) {
                    credentials.removeAt(credentialToDelete)
                    credentialToDelete = -1
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(pv)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start
        ) {

            if (credentials.isNotEmpty()) {
                LazyColumn {
                    items(credentials.size, key = {
                        credentials[it].id
                    }) { i ->
                        val credential = credentials[i]

                        val dismissState = rememberDismissState()

                        if (
                            dismissState.isDismissed(DismissDirection.EndToStart) && !dismissing
                        ) {
                            Log.d("kraken", "why")
                            credentialToDelete = i
                            onDismissCallback = {
                                scope.launch {
                                    Log.d("kraken", "here")
                                    dismissing = true
                                    dismissState.reset()
                                    dismissing = false
                                }
                            }
                        }

                        SwipeToDismiss(
                            state = dismissState,
                            modifier = Modifier
                                .padding(vertical = Dp(1f)),
                            directions = setOf(
                                DismissDirection.EndToStart
                            ),
                            dismissThresholds = { direction ->
                                FractionalThreshold(if (direction == DismissDirection.EndToStart) 0.1f else 0.05f)
                            },
                            background = {
                                val color by animateColorAsState(
                                    when (dismissState.targetValue) {
                                        DismissValue.Default -> Color.White
                                        else -> Color(0xFFDF1B41)
                                    }
                                )

                                val scale by animateFloatAsState(
                                    if (dismissState.targetValue == DismissValue.Default) 0.75f else 1f
                                )

                                Row(
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .fillMaxHeight()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.End,
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            text = "Delete",
                                            fontSize = 16.sp,
                                            fontFamily = epilogueFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.White
                                        )
                                        Gap(width = 12.dp)
                                        Icon(
                                            painterResource(id = R.drawable.ic_delete),
                                            contentDescription = "Delete Icon",
                                            modifier = Modifier.scale(scale),
                                            tint = Color.White
                                        )
                                    }

                                }
                            },
                        ) {
                            CredentialCard(
                                credential = credential,
                                checked = checkedState.value
                            ) {
                                checkedState.value = it
                            }
                        }
                    }
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.empty),
                        contentDescription = "",
                        modifier = Modifier
                            .fillMaxWidth()
                            .size(262.dp),
                        contentScale = ContentScale.Fit
                    )
                    Gap(height = 4.dp)
                    Text(
                        text = "Everything is empty here",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 22.sp,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                    )
                    Gap(height = 8.dp)
                    Text(
                        text = "Add a new account",
                        fontFamily = interFontFamily,
                        color = Color(0xFF545969),
                        textAlign = TextAlign.Center,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun ConfirmDialog(
    modifier: Modifier = Modifier,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        elevation = 8.dp
    ) {
        Column(
            modifier
                .background(Color.White)
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .fillMaxWidth(),
                text = "Are you sure you want to delete this business?",
                textAlign = TextAlign.Start,
                fontFamily = interFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        onCancel()
                    }
                ) {
                    Text(
                        "No",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                        fontFamily = interFontFamily,
                        fontSize = 14.sp
                    )
                }
                TextButton(
                    onClick = {
                        onConfirm()
                    }
                ) {
                    Text(
                        "Yes",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Blue,
                        modifier = Modifier.padding(top = 5.dp, bottom = 5.dp),
                        fontFamily = interFontFamily,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

//@OptIn(ExperimentalMaterialApi::class)
//fun DismissState.test() {
//    currentValue = DismissValue.Default
//}