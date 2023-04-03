package com.orcuspay.dakpion.presentation.screens.filter

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.orcuspay.dakpion.R
import com.orcuspay.dakpion.domain.model.Filter
import com.orcuspay.dakpion.presentation.composables.*
import com.orcuspay.dakpion.presentation.destinations.AddNewFilterScreenDestination
import com.orcuspay.dakpion.presentation.theme.interFontFamily
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterialApi::class)
@Destination
@Composable
fun FilterScreen(
    navigator: DestinationsNavigator,
    viewModel: FilterViewModel = hiltViewModel()
) {

    val filters by viewModel.filtersLiveData.observeAsState(initial = listOf())
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopBar(
                title = "Filters",
                actionButton = {
                    if (filters.isNotEmpty())
                        XButton(
                            text = "Add",
                            style = TextStyle(
                                fontFamily = interFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colors.onPrimary,
                                letterSpacing = 0.sp
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            fillMaxWidth = false,
                            height = 32.dp,
                            modifier = Modifier
                                .padding(end = 16.dp)
                        ) {
                            navigator.navigate(AddNewFilterScreenDestination)
                        }
                }
            )
        },
        floatingActionButton = {
            if (filters.isEmpty())
                FloatingActionButton(
                    onClick = {
                        navigator.navigate(AddNewFilterScreenDestination)
                    },
                    shape = CircleShape,
                    backgroundColor = MaterialTheme.colors.primary,
                    modifier = Modifier
                        .size(86.dp)
                        .offset(y = (-100).dp),
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp
                    )
                ) {
                    Text(
                        text = "Add",
                        fontFamily = interFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colors.onPrimary,
                        letterSpacing = 0.sp
                    )
                }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) { pv ->
        var filterToDelete: Filter? by remember { mutableStateOf(null) }
        var dismissing by remember {
            mutableStateOf(false)
        }
        var onDismissCallback: () -> Unit by remember {
            mutableStateOf({})
        }

        if (filterToDelete != null) {
            Dialog(
                onDismissRequest = {
                    filterToDelete = null
                    onDismissCallback()
                },
                properties = DialogProperties(
                    dismissOnClickOutside = true,
                )
            ) {
                ConfirmDialog(
                    title = "Are you sure you want to delete this filter?",
                    onCancel = {
                        filterToDelete = null
                        onDismissCallback()
                    }
                ) {
                    viewModel.deleteFilter(filterToDelete!!)
                    filterToDelete = null
                    onDismissCallback()
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

            if (filters.isNotEmpty()) {
                LazyColumn {
                    items(filters.size, key = {
                        filters[it].id
                    }) { i ->
                        val filter = filters[i]

                        val dismissState = rememberDismissState()

                        if (
                            dismissState.isDismissed(DismissDirection.EndToStart) && !dismissing
                        ) {
                            filterToDelete = filter
                            onDismissCallback = {
                                scope.launch {
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
                                            fontFamily = interFontFamily,
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
                            FilterCard(
                                filter = filter,
                            ) {
                                viewModel.setFilterEnabled(filter, it)
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
                    Gap(height = 80.dp)
                    Image(
                        painter = painterResource(id = R.drawable.empty),
                        contentDescription = "",
                        modifier = Modifier
                            .size(175.dp),
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
                        text = "You can use filters to exclude messages",
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