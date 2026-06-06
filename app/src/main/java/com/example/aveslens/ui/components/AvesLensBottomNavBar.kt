package com.example.aveslens.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.aveslens.ui.theme.Background
import com.example.aveslens.ui.theme.PrimaryDark
import com.example.aveslens.ui.theme.TextSecondary

@Composable
fun AvesLensBottomNavBar(
    currentRoute: String,
    onJournalClick: () -> Unit,
    onObserveClick: () -> Unit,
    onExplorerClick: () -> Unit,
) {
    NavigationBar(
        containerColor = Background,
        tonalElevation = 0.dp,
    ) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = PrimaryDark,
            selectedTextColor = PrimaryDark,
            indicatorColor = PrimaryDark.copy(alpha = 0.12f),
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary,
        )

        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = onJournalClick,
            icon = { Icon(Icons.AutoMirrored.Outlined.MenuBook, contentDescription = "Journal") },
            label = { Text("Journal") },
            colors = itemColors,
        )
        NavigationBarItem(
            selected = currentRoute == "form",
            onClick = onObserveClick,
            icon = { Icon(Icons.Filled.Add, contentDescription = "Observe") },
            label = { Text("Observe") },
            colors = itemColors,
        )
        NavigationBarItem(
            selected = currentRoute == "explorer",
            onClick = onExplorerClick,
            icon = { Icon(Icons.Outlined.Explore, contentDescription = "Explorer") },
            label = { Text("Explorer") },
            colors = itemColors,
        )
    }
}
