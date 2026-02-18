package com.horsegallop.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.horsegallop.core.theme.*

/**
 * HorseGallop Bottom Navigation Component with Horse Theme Colors
 * - WarmClay (WarmClay) as primary accent color for selected items
 * - ToastedAlmond (ToastedAlmond) as subtle unselected color  
 * - SoftSand (SoftSand) for background harmony
 */
@Composable
fun HorseGallopBottomNavItem(
    icon: @Composable () -> Unit,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    val iconColor = if (selected) WarmClay else SoftSand.copy(alpha = 0.7f)
    val labelColor = if (selected) WarmClay else SoftSand.copy(alpha = 0.7f)

    NavigationBarItem(
        icon = {
            Box(contentAlignment = Alignment.Center) {
                icon()
                
                // Badge if count > 0
                if (badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .background(AppColors.StatusBusy)
                            .clip(CircleShape)
                            .size(16.dp)
                            .padding(start = 12.dp, top = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        },
        label = {
            Text(
                text = label,
                color = labelColor,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 11.sp
            )
        },
        selected = selected,
        onClick = onClick,
        modifier = modifier
    )
}

/**
 * Bottom Navigation Bar with Horse Theme Colors
 * Uses WarmClay as selected color, SoftSand for harmony
 */
@Composable
fun HorseGallopBottomNavigation(
    items: List<BottomNavigationItem>,
    selectedIndex: Int,
    onSelectItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = WarmClay,
        modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            HorseGallopBottomNavItem(
                icon = item.icon,
                label = item.label,
                selected = selectedIndex == index,
                onClick = { onSelectItem(index) },
                badgeCount = item.badgeCount
            )
        }
    }
}

/**
 * Navigation Rail with Horse Theme Colors
 */
@Composable
fun HorseGallopNavigationRail(
    items: List<BottomNavigationItem>,
    selectedIndex: Int,
    onSelectItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationRail(
        containerColor = Color.White,
        contentColor = WarmClay,
        modifier = modifier
    ) {
        items.forEachIndexed { index, item ->
            NavigationRailItem(
                icon = {
                    Box(contentAlignment = Alignment.Center) {
                        item.icon()
                        
                        // Badge if count > 0
                        if (item.badgeCount > 0) {
                            Box(
                                modifier = Modifier
                                    .background(AppColors.StatusBusy)
                                    .clip(CircleShape)
                                    .size(16.dp)
                                    .padding(start = 12.dp, top = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (item.badgeCount > 99) "99+" else item.badgeCount.toString(),
                                    color = Color.White,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        color = if (selectedIndex == index) WarmClay else SoftSand.copy(alpha = 0.7f),
                        fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 11.sp
                    )
                },
                selected = selectedIndex == index,
                onClick = { onSelectItem(index) },
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }
    }
}

/**
 * Represents a bottom navigation item with icon, label and optional badge
 */
data class BottomNavigationItem(
    val icon: @Composable () -> Unit,
    val label: String,
    val route: String,
    val badgeCount: Int = 0
)
