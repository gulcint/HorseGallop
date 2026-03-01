package com.horsegallop.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.horsegallop.ui.theme.LocalSemanticColors
import com.valentinilk.shimmer.shimmer

@Composable
fun HomeDashboardSkeleton() {
    val semantic = LocalSemanticColors.current
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item { WelcomeHeaderSkeleton(cardColor = semantic.cardElevated) }
        item { QuickActionsSkeleton(cardColor = semantic.cardElevated) }
        item { StatsOverviewSkeleton(cardColor = semantic.cardElevated) }
        item { RecentActivitySkeleton(cardColor = semantic.cardElevated) }
        item { TipsSkeleton(cardColor = semantic.cardElevated) }
        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
fun WelcomeHeaderSkeleton(cardColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shimmer(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
        )
    }
}

@Composable
fun QuickActionsSkeleton(cardColor: Color) {
    Column {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(24.dp)
                .shimmer()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(2) {
                Card(
                    modifier = Modifier
                        .width(160.dp)
                        .height(120.dp)
                        .shimmer(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                    )
                }
            }
        }
    }
}

@Composable
fun StatsOverviewSkeleton(cardColor: Color) {
    Column {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(24.dp)
                .shimmer()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shimmer(),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                    )
                }
            }
        }
    }
}

@Composable
fun RecentActivitySkeleton(cardColor: Color) {
    val semantic = LocalSemanticColors.current
    Column {
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(24.dp)
                .shimmer()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shimmer(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                repeat(2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                                    .clip(CircleShape)
                            )
                            Column {
                                Box(
                                    modifier = Modifier
                                        .width(80.dp)
                                        .height(16.dp)
                                        .shimmer()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                                        .clip(RoundedCornerShape(4.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .width(60.dp)
                                        .height(12.dp)
                                        .shimmer()
                                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                                        .clip(RoundedCornerShape(4.dp))
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(14.dp)
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                                    .clip(RoundedCornerShape(4.dp))
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(50.dp)
                                    .height(12.dp)
                                    .shimmer()
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                                    .clip(RoundedCornerShape(4.dp))
                            )
                        }
                    }
                    if (it == 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = semantic.dividerSoft)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TipsSkeleton(cardColor: Color) {
    Column {
        Box(
            modifier = Modifier
                .width(140.dp)
                .height(24.dp)
                .shimmer()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                .clip(RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shimmer(),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shimmer()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                        .clip(CircleShape)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .height(16.dp)
                            .shimmer()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                            .clip(RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .shimmer()
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.62f))
                            .clip(RoundedCornerShape(4.dp))
                    )
                }
            }
        }
    }
}
