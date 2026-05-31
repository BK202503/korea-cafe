package com.harness.cafe.infrastructure.naver

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "naver.local-search")
data class NaverLocalSearchProperties(
    val baseUrl: String = "https://openapi.naver.com",
    val clientId: String = "",
    val clientSecret: String = "",
)
