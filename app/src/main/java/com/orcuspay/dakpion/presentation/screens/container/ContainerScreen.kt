package com.orcuspay.dakpion.presentation.screens.container

import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.orcuspay.dakpion.presentation.NavGraphs
import com.orcuspay.dakpion.presentation.screens.home.BottomBarDestination
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.navigate

@Destination
@Composable
fun ContainerScreen(
    navigator: DestinationsNavigator,
) {

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomBar(navController = navController)
        }
    ) {
        DestinationsNavHost(
            navController = navController,
            navGraph = NavGraphs.root
        )
    }
}


@Composable
fun BottomBar(
    navController: NavController
) {
    var currentDestination =
        BottomBarDestination.Home


    BottomNavigation {
        BottomBarDestination.values().forEach { destination ->
            BottomNavigationItem(
                selected = currentDestination == destination,
                onClick = {
                    currentDestination = destination
                    navController.navigate(destination.direction, fun NavOptionsBuilder.() {
                        launchSingleTop = true
                    })
                },
                icon = { Icon(destination.icon, contentDescription = null) },
                label = { Text(text = destination.label) },
            )
        }
    }
}