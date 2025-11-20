package com.horsegallop.domain.model.content

data class BarnsContent(
  val searchPlaceholder: String,
  val mapTitle: String,
  val resultsPrefix: String,
  val filtersTitle: String? = null,
  val filterLabels: List<String>? = null,
  val emptyTitle: String? = null,
  val emptySubtitle: String? = null
)


