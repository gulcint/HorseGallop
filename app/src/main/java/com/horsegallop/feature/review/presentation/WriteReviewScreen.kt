package com.horsegallop.feature.review.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.horsegallop.R
import com.horsegallop.domain.review.model.ReviewTargetType
import com.horsegallop.ui.theme.LocalSemanticColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteReviewScreen(
    targetId: String,
    targetType: ReviewTargetType,
    targetName: String,
    viewModel: ReviewViewModel = hiltViewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val semantic = LocalSemanticColors.current
    var rating by remember { mutableIntStateOf(0) }
    var comment by remember { mutableStateOf("") }
    var ratingError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.submitSuccess) {
        if (uiState.submitSuccess) {
            viewModel.clearSubmitState()
            onBack()
        }
    }

    Scaffold(
        containerColor = semantic.screenBase,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Değerlendirme Yaz", fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.size(8.dp))

            Text(
                text = targetName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (targetType == ReviewTargetType.INSTRUCTOR) "Eğitmen değerlendirmesi" else "Ders değerlendirmesi",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Puanınız *", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..5).forEach { star ->
                        IconButton(
                            onClick = { rating = star; ratingError = false },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "$star yıldız",
                                tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                if (ratingError) {
                    Text("Lütfen bir puan seçin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }

            OutlinedTextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Yorumunuz") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4,
                maxLines = 8,
                placeholder = { Text("Deneyiminizi paylaşın...") },
                shape = MaterialTheme.shapes.medium
            )

            uiState.submitError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    if (rating == 0) { ratingError = true; return@Button }
                    viewModel.submitReview(targetId, targetType, targetName, rating, comment)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.submitting
            ) {
                if (uiState.submitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    Spacer(Modifier.size(8.dp))
                }
                Text("Değerlendirmeyi Gönder")
            }

            Spacer(Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun WriteReviewContent(
    targetName: String,
    targetType: ReviewTargetType,
    rating: Int,
    comment: String,
    ratingError: Boolean,
    submitError: String?,
    submitting: Boolean,
    onRatingChange: (Int) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.size(8.dp))
        Text(text = targetName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            text = if (targetType == ReviewTargetType.INSTRUCTOR) "Eğitmen değerlendirmesi" else "Ders değerlendirmesi",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Puanınız *", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                (1..5).forEach { star ->
                    IconButton(onClick = { onRatingChange(star) }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "$star yıldız",
                            tint = if (star <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            if (ratingError) {
                Text("Lütfen bir puan seçin", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }
        }
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Yorumunuz") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
            maxLines = 8,
            placeholder = { Text("Deneyiminizi paylaşın...") },
            shape = MaterialTheme.shapes.medium
        )
        submitError?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth(), enabled = !submitting) {
            if (submitting) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                Spacer(Modifier.size(8.dp))
            }
            Text("Değerlendirmeyi Gönder")
        }
        Spacer(Modifier.size(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun WriteReviewScreenPreview() {
    MaterialTheme {
        WriteReviewContent(
            targetName = "Temel Binicilik Dersi",
            targetType = ReviewTargetType.LESSON,
            rating = 4,
            comment = "Harika bir ders deneyimiydi!",
            ratingError = false,
            submitError = null,
            submitting = false,
            onRatingChange = {},
            onCommentChange = {},
            onSubmit = {}
        )
    }
}
