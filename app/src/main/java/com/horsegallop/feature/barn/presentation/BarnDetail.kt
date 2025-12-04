package com.horsegallop.feature.barn.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.horsegallop.theme.AppColors
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay
import androidx.compose.ui.tooling.preview.Preview
import com.horsegallop.feature.home.domain.model.SliderItem
import com.horsegallop.compose.QuickActionCard as CommonQuickActionCard

data class QuickAction(
	val title: String,
	val icon: ImageVector,
	val color: Color,
	val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnDetail(
	slides: List<SliderItem> = emptyList()
) {
	var isLoading by remember { mutableStateOf(true) }
	
	// Simulate initial loading
	LaunchedEffect(Unit) {
		delay(1500) // 1.5 seconds loading
		isLoading = false
	}
	
	if (isLoading) {
		BarnScreenShimmer()
	} else {
		BarnScreenContent(slides)
	}
}

@Preview(showBackground = true, name = "Barn Detail Loaded")
@Composable
private fun PreviewBarnDetail() {
    MaterialTheme {
        val sampleSlides = listOf(
            SliderItem(id = "s1", imageUrl = "https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?w=1200", title = "Majestic Brown", link = null, order = 1),
			SliderItem(id = "s2", imageUrl = "https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=1200", title = "White Portrait", link = null, order = 2),
            SliderItem(id = "s3", imageUrl = "https://images.unsplash.com/photo-1598632640487-6ea4a4e8b963?w=1200", title = "Field Runner", link = null, order = 3)
        )
        BarnScreenContent(slides = sampleSlides)
    }
}

@Preview(showBackground = true, name = "Barn Detail Loading")
@Composable
private fun PreviewBarnScreenLoading() {
    MaterialTheme { BarnScreenShimmer() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnScreenContent(
	slides: List<SliderItem> = emptyList()
) {
	Scaffold(
		modifier = Modifier
			.fillMaxSize(),
		topBar = {
			TopAppBar(
			title = {
				Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        stringResource(com.horsegallop.core.R.string.app_name),
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.Bold
					)
				}
			},
			actions = {
				IconButton(onClick = { }) {
                    Icon(Icons.Default.Notifications, contentDescription = stringResource(com.horsegallop.core.R.string.notifications_description))
				}
				IconButton(onClick = { }) {
                    Icon(Icons.Default.Person, contentDescription = stringResource(com.horsegallop.core.R.string.profile_description))
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = MaterialTheme.colorScheme.surface,
					titleContentColor = MaterialTheme.colorScheme.onSurface
				)
			)
		},
		containerColor = Color(0xFFFAFAFA)
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			contentPadding = PaddingValues(
				start = 16.dp,
				end = 16.dp,
				top = 16.dp,
				bottom = 20.dp
			),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Welcome Banner with Lottie
			item {
				BarnBanner()
			}
			
			// Horse Barn Carousel
			item {
				HorseBarnCarousel()
			}
			
			// Quick Actions
			item {
				QuickActionsSection()
			}
			
			// Featured Slider
			if (slides.isNotEmpty()) {
				item {
					FeaturedSlider(slides)
				}
			}
			
			// Upcoming Lessons
			item {
				UpcomingLessons()
			}
			
			// Restaurant Section
			item {
				RestaurantQuickOrder()
			}
		}
	}
}

@Composable
fun BarnBanner() {
	Card(
		modifier = Modifier
			.fillMaxWidth()
			.height(200.dp),
		shape = RoundedCornerShape(16.dp)
	) {
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.horizontalGradient(
						colors = listOf(
							MaterialTheme.colorScheme.primary,
							MaterialTheme.colorScheme.secondary
						)
					)
				)
		) {
			Row(
				modifier = Modifier
					.fillMaxSize()
					.padding(24.dp),
				verticalAlignment = Alignment.CenterVertically
			) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
                    stringResource(com.horsegallop.core.R.string.welcome_title),
					style = MaterialTheme.typography.headlineMedium,
					fontWeight = FontWeight.Bold,
					color = Color.White
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
                    stringResource(com.horsegallop.core.R.string.welcome_subtitle),
					style = MaterialTheme.typography.bodyLarge,
					color = Color.White.copy(alpha = 0.9f)
				)
				}
				
				// Mini Lottie Animation
				Box(
					modifier = Modifier.size(120.dp),
					contentAlignment = Alignment.Center
				) {
					// Lottie animation (requires app module resource)
					// val composition by rememberLottieComposition(
					//	LottieCompositionSpec.RawRes(R.raw.horse)
					// )
					// Lottie animation placeholder
					Icon(
						Icons.Default.Person,
						contentDescription = "Horse",
						modifier = Modifier.size(80.dp),
						tint = Color.White
					)
				}
			}
		}
	}
}

@Composable
fun QuickActionsSection() {
	Column {
		Text(
            stringResource(com.horsegallop.core.R.string.quick_actions_title),
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold
		)
		Spacer(modifier = Modifier.height(12.dp))
		
    val actions = listOf(
        QuickAction(stringResource(com.horsegallop.core.R.string.action_lesson_reservation), Icons.Default.DateRange, AppColors.ActionLesson),
        QuickAction(stringResource(com.horsegallop.core.R.string.action_my_schedule), Icons.AutoMirrored.Filled.List, AppColors.ActionSchedule),
        QuickAction(stringResource(com.horsegallop.core.R.string.action_restaurant), Icons.Filled.Restaurant, AppColors.ActionRestaurant),
        QuickAction(stringResource(com.horsegallop.core.R.string.action_reviews), Icons.Default.Star, AppColors.ActionReviews)
    )
		
        LazyRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			items(actions) { action ->
                CommonQuickActionCard(
                    title = action.title,
                    icon = action.icon,
                    color = action.color,
                    onClick = action.onClick
                )
			}
		}
	}
}

@Composable
fun FeaturedSlider(slides: List<SliderItem>) {
	Column {
		Text(
            stringResource(com.horsegallop.core.R.string.featured_title),
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold
		)
		Spacer(modifier = Modifier.height(12.dp))
		
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			items(slides) { slide ->
				Card(
					modifier = Modifier
						.width(300.dp)
						.height(180.dp),
					shape = RoundedCornerShape(16.dp)
				) {
					Box {
						AsyncImage(
							model = slide.imageUrl,
							contentDescription = slide.title,
							modifier = Modifier.fillMaxSize(),
							contentScale = ContentScale.Crop
						)
						Box(
							modifier = Modifier
								.fillMaxSize()
								.background(
									Brush.verticalGradient(
										colors = listOf(
											Color.Transparent,
											Color.Black.copy(alpha = 0.7f)
										)
									)
								)
						)
						Text(
							slide.title,
							modifier = Modifier
								.align(Alignment.BottomStart)
								.padding(16.dp),
							style = MaterialTheme.typography.titleMedium,
							fontWeight = FontWeight.Bold,
							color = Color.White
						)
					}
				}
			}
		}
	}
}

@Composable
fun UpcomingLessons() {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.secondaryContainer
		)
	) {
		Column(modifier = Modifier.padding(16.dp)) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
                    stringResource(com.horsegallop.core.R.string.upcoming_lessons_title),
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				TextButton(onClick = { }) {
                    Text(stringResource(com.horsegallop.core.R.string.view_all))
				}
			}
			
			Spacer(modifier = Modifier.height(8.dp))
			
            // Sample lesson
            LessonItem(
                title = stringResource(com.horsegallop.core.R.string.lesson_sample_title),
                instructor = stringResource(com.horsegallop.core.R.string.lesson_sample_instructor),
                date = stringResource(com.horsegallop.core.R.string.lesson_sample_date)
            )
		}
	}
}

@Composable
fun LessonItem(title: String, instructor: String, date: String) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(vertical = 8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Box(
			modifier = Modifier
				.size(48.dp)
				.clip(CircleShape)
				.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				Icons.Default.Add,
				contentDescription = null,
				tint = MaterialTheme.colorScheme.primary
			)
		}
		
		Spacer(modifier = Modifier.width(12.dp))
		
		Column(modifier = Modifier.weight(1f)) {
			Text(
				title,
				style = MaterialTheme.typography.bodyLarge,
				fontWeight = FontWeight.Medium
			)
			Text(
				"$instructor • $date",
				style = MaterialTheme.typography.bodySmall,
				color = MaterialTheme.colorScheme.onSurfaceVariant
			)
		}
		
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = stringResource(com.horsegallop.core.R.string.lesson_detail),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
	}
}

@Composable
fun RestaurantQuickOrder() {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = AppColors.ActionRestaurant.copy(alpha = 0.1f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
				Icon(
					Icons.Filled.Restaurant,
                    contentDescription = stringResource(com.horsegallop.core.R.string.action_restaurant),
					tint = AppColors.ActionRestaurant,
					modifier = Modifier.size(48.dp)
				)
			
			Spacer(modifier = Modifier.width(16.dp))
			
			Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(com.horsegallop.core.R.string.restaurant_order_button),
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				Text(
                    stringResource(com.horsegallop.core.R.string.restaurant_description),
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			
			Button(
				onClick = { },
				colors = ButtonDefaults.buttonColors(
					containerColor = AppColors.ActionRestaurant
				)
			) {
                Text(stringResource(com.horsegallop.core.R.string.restaurant_order_button))
			}
		}
	}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HorseBarnCarousel() {
	// At görselleri - Unsplash'tan yüksek kaliteli at fotoğrafları
	val horseBarnImages = listOf(
		"https://images.unsplash.com/photo-1553284965-83fd3e82fa5a?w=1200&auto=format&fit=crop&q=80", // Beautiful brown horse
		"https://images.unsplash.com/photo-1596464716127-f2a82984de30?w=1200&auto=format&fit=crop&q=80", // White horse portrait
		"https://images.unsplash.com/photo-1598632640487-6ea4a4e8b963?w=1200&auto=format&fit=crop&q=80", // Horse in field
		"https://images.unsplash.com/photo-1551191916-8d837be28e0f?w=1200&auto=format&fit=crop&q=80", // Horse riding scene
		"https://images.unsplash.com/photo-1568572933382-74d440642117?w=1200&auto=format&fit=crop&q=80", // Horse in stable
		"https://images.unsplash.com/photo-1449034446853-66c86144b0ad?w=1200&auto=format&fit=crop&q=80"  // Majestic horse
	)
	
    val pagerState = rememberPagerState(pageCount = { horseBarnImages.size })
    var imageLoaded by remember { mutableStateOf(false) }
    var minSkeletonElapsed by remember { mutableStateOf(false) }

    // En az gösterim süresi
    LaunchedEffect(Unit) {
        delay(800)
        minSkeletonElapsed = true
    }
	
	// Auto-scroll effect - daha yavaş (5 saniye)
    LaunchedEffect(imageLoaded) {
		if (!imageLoaded) return@LaunchedEffect
		while (true) {
			delay(5000)
			val nextPage = (pagerState.currentPage + 1) % horseBarnImages.size
			pagerState.animateScrollToPage(nextPage)
		}
	}
	
	Column {
		Text(
            stringResource(com.horsegallop.core.R.string.carousel_title),
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold,
			modifier = Modifier.padding(bottom = 12.dp)
		)
		
		Card(
			modifier = Modifier
				.fillMaxWidth()
				.height(220.dp),
			shape = RoundedCornerShape(16.dp),
			elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
		) {
			Box(
				modifier = Modifier
					.fillMaxSize()
					.background(Color(0xFFE0E0E0))
			) {
                val showSkeleton = !minSkeletonElapsed

                // Images (hidden while skeleton is showing)
                HorizontalPager(
					state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(alpha = if (showSkeleton) 0f else 1f)
				) { page ->
                    AsyncImage(
                        model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                            .data(horseBarnImages[page])
                            .crossfade(true)
                            .allowHardware(false)
                            .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                            .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                            .addHeader("User-Agent", "horsegallop/1.0 (Android)")
                            .addHeader("Accept", "image/*")
                            .listener(
                                onSuccess = { _, _ -> imageLoaded = true },
                                onError = { _, throwable ->
                                    imageLoaded = true
                                }
                            )
                            .build(),
                        contentDescription = stringResource(com.horsegallop.core.R.string.carousel_image_description, page + 1),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = ColorPainter(Color(0xFFE0E0E0)),
                        error = ColorPainter(Color(0xFFBDBDBD))
                    )
				}

                // Remove extra skeleton overlay to avoid double skeleton after page skeleton ends
				
				// Dark overlay for better text visibility
                if (!showSkeleton) {
					Box(
						modifier = Modifier
							.fillMaxWidth()
							.height(100.dp)
							.align(Alignment.BottomCenter)
							.background(
								Brush.verticalGradient(
									colors = listOf(
										Color.Transparent,
										Color.Black.copy(alpha = 0.4f)
									)
								)
							)
					)

					// Page indicator
					Row(
						Modifier
							.align(Alignment.BottomCenter)
							.padding(16.dp),
						horizontalArrangement = Arrangement.Center
					) {
						repeat(horseBarnImages.size) { iteration ->
							val color = if (pagerState.currentPage == iteration) {
								Color.White
							} else {
								Color.White.copy(alpha = 0.5f)
							}
							Box(
								modifier = Modifier
									.padding(4.dp)
									.clip(CircleShape)
									.background(color)
									.size(8.dp)
							)
						}
					}
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarnScreenShimmer() {
	Scaffold(
		modifier = Modifier
			.fillMaxSize(),
		topBar = {
			TopAppBar(
				title = {
					Box(
						modifier = Modifier
							.width(150.dp)
							.height(28.dp)
							.shimmer()
							.background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
					)
				},
				actions = {
					repeat(2) {
						Box(
							modifier = Modifier
								.padding(8.dp)
								.size(24.dp)
								.shimmer()
								.background(Color(0xFFE0E0E0), CircleShape)
						)
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color(0xFFF5F5F5)
				)
			)
		},
		containerColor = Color(0xFFFAFAFA)
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			contentPadding = PaddingValues(
				start = 16.dp,
				end = 16.dp,
				top = 16.dp,
				bottom = 20.dp
			),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Welcome Banner Shimmer
			item {
				Card(
					modifier = Modifier
						.fillMaxWidth()
						.height(200.dp),
					shape = RoundedCornerShape(16.dp)
				) {
					Box(
						modifier = Modifier
							.fillMaxSize()
							.shimmer()
							.background(
								Brush.horizontalGradient(
									colors = listOf(
										Color(0xFFE0E0E0),
										Color(0xFFF0F0F0)
									)
								)
							)
					)
				}
			}
			
			// Carousel Shimmer
			item {
				Column {
					Box(
						modifier = Modifier
							.width(120.dp)
							.height(28.dp)
							.shimmer()
							.background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
					)
					Spacer(modifier = Modifier.height(12.dp))
					Card(
						modifier = Modifier
							.fillMaxWidth()
							.height(220.dp),
						shape = RoundedCornerShape(16.dp)
					) {
						Box(
							modifier = Modifier
								.fillMaxSize()
								.shimmer()
								.background(Color(0xFFE0E0E0))
						)
					}
				}
			}
			
			// Quick Actions Shimmer
			item {
				Column {
					Box(
						modifier = Modifier
							.width(150.dp)
							.height(28.dp)
							.shimmer()
							.background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
					)
					Spacer(modifier = Modifier.height(16.dp))
					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.spacedBy(12.dp)
					) {
						repeat(4) {
							Card(
								modifier = Modifier
									.weight(1f)
									.height(100.dp),
								shape = RoundedCornerShape(12.dp)
							) {
								Box(
									modifier = Modifier
										.fillMaxSize()
										.shimmer()
										.background(Color(0xFFE0E0E0))
								)
							}
						}
					}
				}
			}
			
			// Upcoming Lessons Shimmer
			item {
				Column {
					Box(
						modifier = Modifier
							.width(180.dp)
							.height(28.dp)
							.shimmer()
							.background(Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
					)
					Spacer(modifier = Modifier.height(16.dp))
					repeat(2) {
						Card(
							modifier = Modifier
								.fillMaxWidth()
								.padding(bottom = 12.dp),
							shape = RoundedCornerShape(12.dp)
						) {
							Row(
								modifier = Modifier
									.fillMaxWidth()
									.height(80.dp)
									.padding(16.dp),
								horizontalArrangement = Arrangement.spacedBy(16.dp)
							) {
								Box(
									modifier = Modifier
										.size(48.dp)
										.shimmer()
										.background(Color(0xFFE0E0E0), CircleShape)
								)
								Column(
									modifier = Modifier.weight(1f),
									verticalArrangement = Arrangement.spacedBy(8.dp)
								) {
									Box(
										modifier = Modifier
											.fillMaxWidth(0.7f)
											.height(16.dp)
											.shimmer()
											.background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
									)
									Box(
										modifier = Modifier
											.fillMaxWidth(0.5f)
											.height(14.dp)
											.shimmer()
											.background(Color(0xFFE0E0E0), RoundedCornerShape(4.dp))
									)
								}
							}
						}
					}
				}
			}
		}
	}
}
