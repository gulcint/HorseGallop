package com.example.feature.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

data class ProfileData(
	val name: String,
	val email: String,
	val phone: String,
	val avatarUrl: String? = null,
	val role: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
	profileData: ProfileData,
	onEditClick: () -> Unit = {},
	onLogoutClick: () -> Unit = {}
) {
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Profil") },
				actions = {
					IconButton(onClick = onEditClick) {
						Icon(Icons.Default.Edit, contentDescription = "Düzenle")
					}
				}
			)
		}
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
				.padding(16.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			// Avatar
			Box(
				modifier = Modifier
					.size(120.dp)
					.clip(CircleShape)
					.background(MaterialTheme.colorScheme.primaryContainer),
				contentAlignment = Alignment.Center
			) {
				if (profileData.avatarUrl != null) {
					Image(
						painter = rememberAsyncImagePainter(profileData.avatarUrl),
						contentDescription = "Avatar",
						modifier = Modifier.fillMaxSize(),
						contentScale = ContentScale.Crop
					)
				} else {
					Icon(
						imageVector = Icons.Default.Person,
						contentDescription = "Avatar",
						modifier = Modifier.size(60.dp),
						tint = MaterialTheme.colorScheme.onPrimaryContainer
					)
				}
			}
			
			Spacer(modifier = Modifier.height(16.dp))
			
			// Name
			Text(
				text = profileData.name,
				style = MaterialTheme.typography.headlineMedium
			)
			
			// Role
			Text(
				text = profileData.role,
				style = MaterialTheme.typography.bodyMedium,
				color = MaterialTheme.colorScheme.secondary
			)
			
			Spacer(modifier = Modifier.height(32.dp))
			
			// Info Cards
			ProfileInfoItem(
				icon = Icons.Default.Email,
				label = "E-posta",
				value = profileData.email
			)
			
			Spacer(modifier = Modifier.height(12.dp))
			
			ProfileInfoItem(
				icon = Icons.Default.Phone,
				label = "Telefon",
				value = profileData.phone
			)
			
			Spacer(modifier = Modifier.weight(1f))
			
			// Logout Button
			Button(
				onClick = onLogoutClick,
				modifier = Modifier.fillMaxWidth(),
				colors = ButtonDefaults.buttonColors(
					containerColor = MaterialTheme.colorScheme.error
				)
			) {
				Text("Çıkış Yap")
			}
		}
	}
}

@Composable
fun ProfileInfoItem(
	icon: ImageVector,
	label: String,
	value: String
) {
	Card(
		modifier = Modifier.fillMaxWidth(),
		colors = CardDefaults.cardColors(
			containerColor = MaterialTheme.colorScheme.surfaceVariant
		)
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = icon,
				contentDescription = label,
				tint = MaterialTheme.colorScheme.primary
			)
			
			Spacer(modifier = Modifier.width(16.dp))
			
			Column {
				Text(
					text = label,
					style = MaterialTheme.typography.labelSmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
				)
				Text(
					text = value,
					style = MaterialTheme.typography.bodyLarge
				)
			}
		}
	}
}

@Preview
@Composable
private fun ProfilePreview() {
	MaterialTheme {
		ProfileScreen(
			profileData = ProfileData(
				name = "Ahmet Yılmaz",
				email = "ahmet@example.com",
				phone = "+90 555 123 4567",
				role = "Müşteri"
			)
		)
	}
}
