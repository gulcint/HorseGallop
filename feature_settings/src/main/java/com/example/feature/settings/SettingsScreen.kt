package com.example.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
	currentLanguage: String = "tr",
	onLanguageChange: (String) -> Unit = {},
	notificationsEnabled: Boolean = true,
	onNotificationsChange: (Boolean) -> Unit = {},
	onAboutClick: () -> Unit = {},
	onPrivacyClick: () -> Unit = {},
	onTermsClick: () -> Unit = {}
) {
	var showLanguageDialog by remember { mutableStateOf(false) }
	
	Scaffold(
		topBar = {
			TopAppBar(
				title = { Text("Ayarlar") }
			)
		}
	) { padding ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(padding)
		) {
			// Language Section
			SettingsSection(title = "Genel")
			
			SettingsItem(
				icon = Icons.Default.Settings,
				title = "Dil",
				subtitle = if (currentLanguage == "tr") "Türkçe" else "English",
				onClick = { showLanguageDialog = true }
			)
			
			Divider()
			
			// Notifications
			SettingsSection(title = "Bildirimler")
			
			SettingsSwitchItem(
				icon = Icons.Default.Notifications,
				title = "Push Bildirimleri",
				subtitle = "Yeni ders ve etkinlik bildirimleri",
				checked = notificationsEnabled,
				onCheckedChange = onNotificationsChange
			)
			
			Divider()
			
			// About Section
			SettingsSection(title = "Hakkında")
			
			SettingsItem(
				icon = Icons.Default.Info,
				title = "Uygulama Hakkında",
				subtitle = "Versiyon 1.0.0",
				onClick = onAboutClick
			)
			
			SettingsItem(
				icon = Icons.Default.Info,
				title = "Gizlilik Politikası",
				onClick = onPrivacyClick
			)
			
			SettingsItem(
				icon = Icons.Default.Info,
				title = "Kullanım Koşulları",
				onClick = onTermsClick
			)
		}
	}
	
	if (showLanguageDialog) {
		LanguageDialog(
			currentLanguage = currentLanguage,
			onDismiss = { showLanguageDialog = false },
			onLanguageSelected = { lang ->
				onLanguageChange(lang)
				showLanguageDialog = false
			}
		)
	}
}

@Composable
fun SettingsSection(title: String) {
	Text(
		text = title,
		style = MaterialTheme.typography.labelMedium,
		color = MaterialTheme.colorScheme.primary,
		modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
	)
}

@Composable
fun SettingsItem(
	icon: ImageVector,
	title: String,
	subtitle: String? = null,
	onClick: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onClick)
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = title,
			tint = MaterialTheme.colorScheme.primary
		)
		
		Spacer(modifier = Modifier.width(16.dp))
		
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
				)
			}
		}
		
		Text(
			text = ">",
			style = MaterialTheme.typography.bodyLarge,
			color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
		)
	}
}

@Composable
fun SettingsSwitchItem(
	icon: ImageVector,
	title: String,
	subtitle: String? = null,
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.padding(16.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Icon(
			imageVector = icon,
			contentDescription = title,
			tint = MaterialTheme.colorScheme.primary
		)
		
		Spacer(modifier = Modifier.width(16.dp))
		
		Column(modifier = Modifier.weight(1f)) {
			Text(
				text = title,
				style = MaterialTheme.typography.bodyLarge
			)
			if (subtitle != null) {
				Text(
					text = subtitle,
					style = MaterialTheme.typography.bodySmall,
					color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
				)
			}
		}
		
		Switch(
			checked = checked,
			onCheckedChange = onCheckedChange
		)
	}
}

@Composable
fun LanguageDialog(
	currentLanguage: String,
	onDismiss: () -> Unit,
	onLanguageSelected: (String) -> Unit
) {
	AlertDialog(
		onDismissRequest = onDismiss,
		title = { Text("Dil Seçin") },
		text = {
			Column {
				LanguageOption(
					language = "tr",
					label = "Türkçe",
					isSelected = currentLanguage == "tr",
					onSelect = { onLanguageSelected("tr") }
				)
				Spacer(modifier = Modifier.height(8.dp))
				LanguageOption(
					language = "en",
					label = "English",
					isSelected = currentLanguage == "en",
					onSelect = { onLanguageSelected("en") }
				)
			}
		},
		confirmButton = {
			TextButton(onClick = onDismiss) {
				Text("Kapat")
			}
		}
	)
}

@Composable
fun LanguageOption(
	language: String,
	label: String,
	isSelected: Boolean,
	onSelect: () -> Unit
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clickable(onClick = onSelect)
			.padding(8.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		RadioButton(
			selected = isSelected,
			onClick = onSelect
		)
		Spacer(modifier = Modifier.width(8.dp))
		Text(text = label)
	}
}

@Preview
@Composable
private fun SettingsPreview() {
	MaterialTheme {
		SettingsScreen()
	}
}
