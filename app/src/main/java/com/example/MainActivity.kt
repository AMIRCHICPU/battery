package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.ShowChart
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DesignStyle
import com.example.data.model.WidgetConfig
import com.example.data.preferences.WidgetPreferences
import com.example.service.BatteryLiveService
import com.example.service.BatteryNotificationManager
import com.example.ui.screens.GalleryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.InsightsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.ProfilesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudioScreen
import com.example.ui.screens.TroubleshootingScreen
import com.example.ui.theme.VoltTheme

enum class NavDestination(val label: String) {
    HOME("Home"),
    GALLERY("Gallery"),
    STUDIO("Studio"),
    INSIGHTS("Insights"),
    MORE("More")
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        BatteryNotificationManager.createNotificationChannels(this)

        val prefs = WidgetPreferences(this)
        if (prefs.isLiveModeEnabled()) {
            BatteryLiveService.startService(this)
        }

        setContent {
            VoltTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentDestination by remember { mutableStateOf(NavDestination.HOME) }
    var studioInitialStyle by remember { mutableStateOf<DesignStyle?>(null) }
    var moreSubTab by remember { mutableIntStateOf(0) }

    BackHandler(enabled = currentDestination != NavDestination.HOME) {
        currentDestination = NavDestination.HOME
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = currentDestination == NavDestination.HOME,
                    onClick = { currentDestination = NavDestination.HOME },
                    icon = {
                        Icon(
                            imageVector = if (currentDestination == NavDestination.HOME) Icons.Filled.Bolt else Icons.Outlined.Bolt,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == NavDestination.GALLERY,
                    onClick = { currentDestination = NavDestination.GALLERY },
                    icon = {
                        Icon(
                            imageVector = if (currentDestination == NavDestination.GALLERY) Icons.Filled.Widgets else Icons.Outlined.Widgets,
                            contentDescription = "Gallery"
                        )
                    },
                    label = { Text("Gallery") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == NavDestination.STUDIO,
                    onClick = {
                        studioInitialStyle = null
                        currentDestination = NavDestination.STUDIO
                    },
                    icon = {
                        Icon(
                            imageVector = if (currentDestination == NavDestination.STUDIO) Icons.Filled.Tune else Icons.Outlined.Tune,
                            contentDescription = "Studio"
                        )
                    },
                    label = { Text("Studio") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == NavDestination.INSIGHTS,
                    onClick = { currentDestination = NavDestination.INSIGHTS },
                    icon = {
                        Icon(
                            imageVector = if (currentDestination == NavDestination.INSIGHTS) Icons.Filled.ShowChart else Icons.Outlined.ShowChart,
                            contentDescription = "Insights"
                        )
                    },
                    label = { Text("Insights") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentDestination == NavDestination.MORE,
                    onClick = { currentDestination = NavDestination.MORE },
                    icon = {
                        Icon(
                            imageVector = if (currentDestination == NavDestination.MORE) Icons.Filled.Settings else Icons.Outlined.Settings,
                            contentDescription = "Settings"
                        )
                    },
                    label = { Text("More") },
                    colors = NavigationBarItemDefaults.colors(
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                NavDestination.HOME -> {
                    HomeScreen(
                        onNavigateToStudio = { style ->
                            studioInitialStyle = style
                            currentDestination = NavDestination.STUDIO
                        },
                        onNavigateToGallery = {
                            currentDestination = NavDestination.GALLERY
                        },
                        onNavigateToTroubleshooting = {
                            moreSubTab = 3
                            currentDestination = NavDestination.MORE
                        }
                    )
                }

                NavDestination.GALLERY -> {
                    GalleryScreen(
                        onNavigateToStudio = { style ->
                            studioInitialStyle = style
                            currentDestination = NavDestination.STUDIO
                        }
                    )
                }

                NavDestination.STUDIO -> {
                    StudioScreen(initialStyle = studioInitialStyle)
                }

                NavDestination.INSIGHTS -> {
                    InsightsScreen()
                }

                NavDestination.MORE -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        ScrollableTabRow(
                            selectedTabIndex = moreSubTab,
                            containerColor = MaterialTheme.colorScheme.surface,
                            edgePadding = 16.dp
                        ) {
                            Tab(
                                selected = moreSubTab == 0,
                                onClick = { moreSubTab = 0 },
                                text = { Text("Update Mode & Diagnostics") }
                            )
                            Tab(
                                selected = moreSubTab == 1,
                                onClick = { moreSubTab = 1 },
                                text = { Text("Saved Profiles") }
                            )
                            Tab(
                                selected = moreSubTab == 2,
                                onClick = { moreSubTab = 2 },
                                text = { Text("Alerts & Thresholds") }
                            )
                            Tab(
                                selected = moreSubTab == 3,
                                onClick = { moreSubTab = 3 },
                                text = { Text("Samsung & One UI Guide") }
                            )
                        }

                        when (moreSubTab) {
                            0 -> SettingsScreen()
                            1 -> ProfilesScreen(
                                onEditInStudio = { profile ->
                                    studioInitialStyle = profile.style
                                    currentDestination = NavDestination.STUDIO
                                }
                            )
                            2 -> NotificationsScreen()
                            3 -> TroubleshootingScreen()
                        }
                    }
                }
            }
        }
    }
}
