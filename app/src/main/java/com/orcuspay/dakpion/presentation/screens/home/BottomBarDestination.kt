package com.orcuspay.dakpion.presentation.screens.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector
import com.orcuspay.dakpion.presentation.destinations.HomeScreenDestination
import com.orcuspay.dakpion.presentation.destinations.SMSLogScreenDestination
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec

enum class BottomBarDestination(
    val direction: DirectionDestinationSpec,
    val icon: ImageVector,
    val label: String,
) {
    Home(HomeScreenDestination, Icons.Default.Home, "Home"),
    Log(SMSLogScreenDestination, Icons.Default.Edit, "Logs"),
}