@file:OptIn(ExperimentalMaterial3Api::class)

package com.horsegallop.feature.auth.presentation

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.core.components.HorseLoadingOverlay
import com.horsegallop.core.feedback.LocalAppFeedbackController
import com.horsegallop.domain.auth.model.UserProfile
import com.horsegallop.ui.theme.AppTheme
import com.horsegallop.ui.theme.LocalSemanticColors

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onSettings: () -> Unit,
    onLogout: () -> Unit,
    onEditProfile: () -> Unit,
    onNotifications: () -> Unit = {},
    onSchedule: () -> Unit = {},
    onChallenges: () -> Unit = {},
    onTbfEvents: () -> Unit = {},
    onMyReviews: () -> Unit = {},
    onMyBarn: (barnId: String) -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val semantic = LocalSemanticColors.current
    val feedback = LocalAppFeedbackController.current

    val pickMediaLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri != null) viewModel.updateProfileImage(uri)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted: Boolean ->
        if (granted) {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    val openImagePicker: () -> Unit = {
        if (Build.VERSION.SDK_INT >= 33) {
            pickMediaLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    val profile = state.userProfile
    val fullName = listOf(profile.firstName, profile.lastName)
        .filter { it.isNotBlank() }
        .joinToString(" ")
        .ifBlank { context.getString(R.string.default_user_name) }

    LaunchedEffect(state.errorMessageResId) {
        state.errorMessageResId?.let {
            feedback.showError(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(state.successMessageResId) {
        state.successMessageResId?.let {
            feedback.showSuccess(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(id = R.string.profile),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotifications) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = stringResource(R.string.profile_notifications_cd)
                        )
                    }
                    IconButton(onClick = onSettings) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = stringResource(id = R.string.settings)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = semantic.screenTopBar,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1 — Hero banner (avatar + name + email + city + inline stats)
            item {
                ProfileHeroCard(
                    profile = profile,
                    fullName = fullName,
                    totalRides = state.totalRides,
                    totalKm = state.totalKm,
                    totalHours = state.totalHours,
                    avgRating = state.avgRating,
                    onPhotoClick = openImagePicker
                )
            }

            // 2 — Atlarım mini listesi
            item {
                HorsesMiniCard(
                    horses = state.myHorses,
                    onSeeAll = {}
                )
            }

            // 3 — Hesap Bilgileri (sadece phone, doğum tarihi, kilo)
            item {
                ProfileSectionCard(title = stringResource(id = R.string.profile_snapshot_title)) {
                    ProfileInfoRow(
                        icon = Icons.Filled.Phone,
                        label = stringResource(id = R.string.label_phone),
                        value = formatMaskedPhone(profile.countryCode, profile.phone)
                    )
                    ProfileInfoRow(
                        icon = Icons.Filled.CalendarToday,
                        label = stringResource(id = R.string.label_birth_date),
                        value = profile.birthDate
                    )
                    ProfileInfoRow(
                        icon = Icons.Filled.MonitorWeight,
                        label = stringResource(id = R.string.label_weight),
                        value = formatWeight(profile.weight)
                    )
                }
            }

            // 4 — Eylemler (gradient CTA + list items)
            item {
                ProfileActionsCard(
                    onEditProfile = {
                        viewModel.startEditSession(force = true)
                        onEditProfile()
                    },
                    onLogout = { viewModel.signOut(onLogout) },
                    onSchedule = onSchedule,
                    onChallenges = onChallenges,
                    onTbfEvents = onTbfEvents,
                    onMyReviews = onMyReviews,
                    ownedBarnId = state.ownedBarnId,
                    onMyBarn = onMyBarn
                )
            }

            item {
                Spacer(
                    modifier = Modifier.height(
                        WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding() + 16.dp
                    )
                )
            }
        }
    }

    HorseLoadingOverlay(visible = state.isLoading || state.isSaving)
}

@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    AppTheme {
        val fakeProfile = UserProfile(
            firstName = "Ayşe",
            lastName = "Yılmaz",
            email = "ayse@example.com",
            city = "İstanbul",
            phone = "5551234567",
            countryCode = "+90",
            birthDate = "15/03/1990",
            weight = 62f
        )
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                ProfileHeroCard(
                    profile = fakeProfile,
                    fullName = "Ayşe Yılmaz",
                    totalRides = 24,
                    totalKm = 312.5,
                    totalHours = 18.3,
                    avgRating = 4.7,
                    onPhotoClick = {}
                )
            }
            item {
                HorsesMiniCard(
                    horses = emptyList(),
                    onSeeAll = {}
                )
            }
            item {
                ProfileSectionCard(title = "Hesap Bilgileri") {
                    ProfileInfoRow(Icons.Filled.Phone, "Telefon", "+90 555 123 45 67")
                    ProfileInfoRow(Icons.Filled.CalendarToday, "Doğum Tarihi", "15/03/1990")
                    ProfileInfoRow(Icons.Filled.MonitorWeight, "Kilo", "62 kg")
                }
            }
            item {
                ProfileActionsCard(
                    onEditProfile = {},
                    onLogout = {}
                )
            }
        }
    }
}
