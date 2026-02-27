package com.horsegallop.core.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import com.horsegallop.ui.theme.LocalSemanticColors
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun HorseLoadingOverlay(visible: Boolean) {
  if (!visible) return
  val semantic = LocalSemanticColors.current
  Dialog(
    onDismissRequest = {},
    properties = DialogProperties(
      dismissOnBackPress = false,
      dismissOnClickOutside = false,
      usePlatformDefaultWidth = false
    )
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(semantic.panelOverlay.copy(alpha = 0.22f)),
      contentAlignment = Alignment.Center
    ) {
      val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(com.horsegallop.R.raw.horse))
      val progress by animateLottieCompositionAsState(composition, iterations = LottieConstants.IterateForever)
      LottieAnimation(composition = composition, progress = { progress }, modifier = Modifier.fillMaxSize(fraction = 0.3f))
    }
  }
}
