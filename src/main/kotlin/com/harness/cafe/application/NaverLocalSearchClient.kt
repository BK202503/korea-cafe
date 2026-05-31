package com.harness.cafe.application

import com.fasterxml.jackson.annotation.JsonProperty

interface NaverLocalSearchClient {
    fun search(query: String): NaverLocalSearchResponse
}

data class NaverLocalSearchResponse(
    val lastBuildDate: String? = null,
    val total: Int = 0,
    val start: Int = 0,
    val display: Int = 0,
    val items: List<NaverLocalSearchItem> = emptyList(),
)

data class NaverLocalSearchItem(
    val title: String,
    val link: String,
    val category: String,
    val description: String,
    val telephone: String,
    val address: String,
    val roadAddress: String,
    @JsonProperty("mapx")
    val mapX: Long,
    @JsonProperty("mapy")
    val mapY: Long,
)
