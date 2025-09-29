package com.example.feature_home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.core.localization.LocalizedContent
import com.example.domain.model.SliderItem
import kotlinx.coroutines.delay

@Composable
fun HomeSlider(
  slides: List<SliderItem>,
  autoScrollMs: Long = 3000
) {
  val index = remember { mutableIntStateOf(0) }
  LaunchedEffect(slides) {
    while (slides.isNotEmpty()) {
      delay(autoScrollMs)
      index.intValue = (index.intValue + 1) % slides.size
    }
  }
  LazyRow {
    items(slides) { item ->
      Card {
        Box(modifier = Modifier.fillMaxWidth().height(180.dp).background(Color.Black)) {
          AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title
          )
        }
      }
    }
  }
}

@Composable
fun HomeScreen(
  slides: List<SliderItem>
) {
  Text(text = LocalizedContent.getString(com.example.core.R.string.home_title), style = MaterialTheme.typography.headlineSmall)
  HomeSlider(slides = slides)
}

@Preview
@Composable
private fun HomePreview() {
  HomeScreen(
    slides = listOf(
      SliderItem(id = "s1", imageUrl = "https://picsum.photos/800/400", title = "Camp", link = null, order = 1),
      SliderItem(id = "s2", imageUrl = "https://picsum.photos/800/401", title = "Ride", link = null, order = 2)
    )
  )
}
