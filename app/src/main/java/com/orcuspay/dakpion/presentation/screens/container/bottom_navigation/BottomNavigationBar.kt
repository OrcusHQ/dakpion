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
import com.orcuspay.dakpion.presentation.theme.PrimaryColor
import com.orcuspay.dakpion.presentation.theme.interFontFamily

val NavigationItems = listOf(
    NavigationItem("home", R.drawable.ic_home, "Home"),
    NavigationItem("analytics", R.drawable.ic_notification, "Analytics"),
    NavigationItem("logs", R.drawable.ic_clipboard, "Logs"),
    NavigationItem("filters", R.drawable.ic_filter, "Filters"),
    NavigationItem("help", R.drawable.ic_alert, "Help"),
)

@Composable
fun BottomNavigationBar(
    backgroundColor: Color = Color.White,
    navController: NavController,
) {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = backgroundColor,
        contentColor = Color.White,
        elevation = 12.dp,
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
                    BoxWithConstraints {
                        Text(
                            modifier = Modifier
                                .wrapContentWidth(unbounded = true)
                                .requiredWidth(maxWidth + 24.dp),
                            text = navigationItem.title,
                            softWrap = false,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            fontFamily = interFontFamily,
                        )
                    }
                },
                selectedContentColor = PrimaryColor,
                unselectedContentColor = Color(0xFFB0B8C1),
                alwaysShowLabel = true,
                selected = selected,
                onClick = {
                    navController.navigate(navigationItem.route) {
                        navController.graph.startDestinationRoute?.let { route ->
                            popUpTo(route) {
                                saveState = true
                            }
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}