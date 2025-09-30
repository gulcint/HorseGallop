package com.example.feature_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.*
import com.example.domain.model.SliderItem
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay

data class QuickAction(
	val title: String,
	val icon: ImageVector,
	val color: Color,
	val onClick: () -> Unit = {}
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
	slides: List<SliderItem> = emptyList()
) {
	Scaffold(
		modifier = Modifier
			.fillMaxSize()
			.statusBarsPadding()
			.navigationBarsPadding(),
		topBar = {
			TopAppBar(
				title = {
					Row(verticalAlignment = Alignment.CenterVertically) {
						Text(
							"Adin Country",
							style = MaterialTheme.typography.headlineMedium,
							fontWeight = FontWeight.Bold
						)
					}
				},
				actions = {
					IconButton(onClick = { }) {
						Icon(Icons.Default.Notifications, contentDescription = "Bildirimler")
					}
					IconButton(onClick = { }) {
						Icon(Icons.Default.Person, contentDescription = "Profil")
					}
				},
				colors = TopAppBarDefaults.topAppBarColors(
					containerColor = Color(0xFFF5F5F5),
					titleContentColor = Color(0xFF1A1A1A)
				)
			)
		},
		containerColor = Color(0xFFFAFAFA)
	) { padding ->
		LazyColumn(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding),
			contentPadding = PaddingValues(16.dp),
			verticalArrangement = Arrangement.spacedBy(24.dp)
		) {
			// Welcome Banner with Lottie
			item {
				WelcomeBanner()
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
fun WelcomeBanner() {
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
						"Hoş Geldiniz!",
						style = MaterialTheme.typography.headlineMedium,
						fontWeight = FontWeight.Bold,
						color = Color.White
					)
					Spacer(modifier = Modifier.height(8.dp))
					Text(
						"At binicilik maceranız burada başlıyor",
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
			"Hızlı İşlemler",
			style = MaterialTheme.typography.titleLarge,
			fontWeight = FontWeight.Bold
		)
		Spacer(modifier = Modifier.height(12.dp))
		
	val actions = listOf(
		QuickAction("Ders Rezervasyonu", Icons.Default.DateRange, Color(0xFF4CAF50)),
		QuickAction("Programım", Icons.Default.List, Color(0xFF2196F3)),
		QuickAction("Restoran", Icons.Default.ShoppingCart, Color(0xFFFF9800)),
		QuickAction("Yorumlar", Icons.Default.Star, Color(0xFFF44336))
	)
		
		LazyRow(
			horizontalArrangement = Arrangement.spacedBy(12.dp)
		) {
			items(actions) { action ->
				QuickActionCard(action)
			}
		}
	}
}

@Composable
fun QuickActionCard(action: QuickAction) {
	Card(
		modifier = Modifier
			.width(140.dp)
			.height(140.dp),
		onClick = action.onClick,
		colors = CardDefaults.cardColors(
			containerColor = action.color.copy(alpha = 0.1f)
		),
		shape = RoundedCornerShape(16.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Center
		) {
			Box(
				modifier = Modifier
					.size(56.dp)
					.clip(CircleShape)
					.background(action.color.copy(alpha = 0.2f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					action.icon,
					contentDescription = action.title,
					tint = action.color,
					modifier = Modifier.size(32.dp)
				)
			}
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				action.title,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.Medium,
				color = action.color
			)
		}
	}
}

@Composable
fun FeaturedSlider(slides: List<SliderItem>) {
	Column {
		Text(
			"Öne Çıkanlar",
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
					"Yaklaşan Dersler",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				TextButton(onClick = { }) {
					Text("Tümünü Gör")
				}
			}
			
			Spacer(modifier = Modifier.height(8.dp))
			
			// Sample lesson
			LessonItem(
				title = "Başlangıç At Binme",
				instructor = "Ahmet Yılmaz",
				date = "1 Ekim, 14:00"
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
			Icons.Default.ArrowForward,
			contentDescription = "Detay",
			tint = MaterialTheme.colorScheme.onSurfaceVariant
		)
	}
}

@Composable
fun RestaurantQuickOrder() {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				Icons.Default.ShoppingCart,
				contentDescription = "Restoran",
				tint = Color(0xFFFF9800),
				modifier = Modifier.size(48.dp)
			)
			
			Spacer(modifier = Modifier.width(16.dp))
			
			Column(modifier = Modifier.weight(1f)) {
				Text(
					"Restorandan Sipariş Ver",
					style = MaterialTheme.typography.titleMedium,
					fontWeight = FontWeight.Bold
				)
				Text(
					"Taze lezzetler sizi bekliyor",
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant
				)
			}
			
			Button(
				onClick = { },
				colors = ButtonDefaults.buttonColors(
					containerColor = Color(0xFFFF9800)
				)
			) {
				Text("Sipariş Ver")
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
	var isLoading by remember { mutableStateOf(true) }
	var imageLoaded by remember { mutableStateOf(false) }
	
	// Minimum loading time to show skeleton
	LaunchedEffect(Unit) {
		delay(800) // Minimum 800ms skeleton göster
		if (imageLoaded) {
			isLoading = false
		}
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
			"Atlarımız",
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
				if (isLoading) {
					SkeletonCarousel()
				}
				
				HorizontalPager(
					state = pagerState,
					modifier = Modifier.fillMaxSize()
				) { page ->
					AsyncImage(
						model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
							.data(horseBarnImages[page])
							.crossfade(true)
							.diskCachePolicy(coil.request.CachePolicy.ENABLED)
							.memoryCachePolicy(coil.request.CachePolicy.ENABLED)
							.listener(
								onSuccess = { _, _ -> 
									imageLoaded = true
									isLoading = false
								}
							)
							.build(),
						contentDescription = "At Görseli ${page + 1}",
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop
					)
				}
				
				// Dark overlay for better text visibility
				if (!isLoading) {
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

@Composable
fun SkeletonCarousel() {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.shimmer()
	) {
		// Background gradient
		Box(
			modifier = Modifier
				.fillMaxSize()
				.background(
					Brush.verticalGradient(
						colors = listOf(
							Color(0xFFE8E8E8),
							Color(0xFFF5F5F5),
							Color(0xFFE8E8E8)
						)
					)
				)
		)
		
		// Skeleton shapes
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(24.dp),
			verticalArrangement = Arrangement.SpaceBetween,
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			// Top shimmer bar
			Box(
				modifier = Modifier
					.fillMaxWidth(0.6f)
					.height(24.dp)
					.background(Color(0xFFD0D0D0), RoundedCornerShape(12.dp))
			)
			
			// Center icon placeholder
			Box(
				modifier = Modifier
					.size(80.dp)
					.background(Color(0xFFD0D0D0), CircleShape)
			)
			
			// Bottom shimmer indicators
			Row(
				horizontalArrangement = Arrangement.Center
			) {
				repeat(6) {
					Box(
						modifier = Modifier
							.padding(4.dp)
							.size(10.dp)
							.background(Color(0xFFD0D0D0), CircleShape)
					)
				}
			}
		}
	}
}
