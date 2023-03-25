package com.orcuspay.dakpion.presentation.screens.container

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.orcuspay.dakpion.presentation.screens.container.bottom_navigation.BottomNavigationBar
import com.orcuspay.dakpion.presentation.screens.container.bottom_navigation.NavigationItems
import com.orcuspay.dakpion.presentation.screens.home.HomeScreen
import com.orcuspay.dakpion.presentation.screens.logs.SMSLogScreen
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination
@Composable
fun ContainerScreen(
    navigator: DestinationsNavigator,
) {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
            )
        }
    ) {
        Box(modifier = Modifier.padding(it)) {
            Navigation(navController, navigator)
        }
    }
}


@Composable
fun Navigation(navController: NavHostController, navigator: DestinationsNavigator) {
    NavHost(navController, startDestination = NavigationItems.first().route) {

        composable(NavigationItems.first().route) {
            HomeScreen(navigator = navigator)
        }
        composable(NavigationItems.last().route) {
            SMSLogScreen(navigator = navigator)
        }
    }
}