package com.orcuspay.dakpion.presentation.screens.container.bottom_navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.orcuspay.dakpion.R

val NavigationItems = listOf(
    NavigationItem("home", R.drawable.ic_home, "Home"),
    NavigationItem("logs", R.drawable.ic_clipboard, "Logs"),
)

@Composable
fun BottomNavigationBar(
    backgroundColor: Color = Color.White,
    navController: NavController,
) {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = backgroundColor,
        contentColor = Color.White
    ) {

        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationItems.forEach { navigationItem ->

            val selected = currentRoute == navigationItem.route

            BottomNavigationItem(
                icon = {
                    BoxWithConstraints(
                        modifier = Modifier
                            .wrapContentWidth(unbounded = true)
                            .requiredWidth(34.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painterResource(id = navigationItem.icon),
                            contentDescription = null
                        )
                    }
                },
                label = {
//                    Text(
//                        text = navigationItem.title,
//                        maxLines = 1,
//                        fontSize = 10.sp,
//                        fontWeight = FontWeight(500),
//                        softWrap = false
//                    )
                    BoxWithConstraints {
                        Text(
                            modifier = Modifier
                                .wrapContentWidth(unbounded = true)
                                .requiredWidth(maxWidth + 24.dp), // 24.dp = the padding * 2
                            text = navigationItem.title,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = 10.sp,
                            fontWeight = FontWeight(500),
                        )
                    }
                },
                selectedContentColor = MaterialTheme.colors.primary,
                unselectedContentColor = MaterialTheme.colors.secondary,
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigate(navigationItem.route) {
                        // Pop up to the start destination of the graph to
                        // avoid building up a large stack of destinations
                        // on the back stack as users select items
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        // Avoid multiple copies of the same destination when
                        // reselecting the same item
                        launchSingleTop = true
                        // Restore state when reselecting a previously selected item
                        restoreState = true
                    }
                }
            )
        }
    }
}