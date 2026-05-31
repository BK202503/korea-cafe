package com.harness.cafe.infrastructure.naver

import com.harness.cafe.application.NaverLocalSearchClient
import com.harness.cafe.application.NaverLocalSearchResponse
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import tools.jackson.databind.ObjectMapper

@Component
@EnableConfigurationProperties(NaverLocalSearchProperties::class)
class NaverLocalSearchClientImpl(
    private val properties: NaverLocalSearchProperties,
    private val objectMapper: ObjectMapper,
) : NaverLocalSearchClient {
    private val restClient: RestClient = RestClient.builder().baseUrl(properties.baseUrl).build()

    override fun search(query: String): NaverLocalSearchResponse {
        if (properties.clientId.isBlank()) {
            return loadStubFixture()
        }
        return restClient
            .get()
            .uri { builder ->
                builder
                    .path("/v1/search/local.json")
                    .queryParam("query", query)
                    .queryParam("display", 5)
                    .queryParam("start", 1)
                    .queryParam("sort", "random")
                    .build()
            }.header("X-Naver-Client-Id", properties.clientId)
            .header("X-Naver-Client-Secret", properties.clientSecret)
            .accept(MediaType.APPLICATION_JSON)
            .retrieve()
            .body(NaverLocalSearchResponse::class.java)
            ?: NaverLocalSearchResponse()
    }

    private fun loadStubFixture(): NaverLocalSearchResponse {
        val resource = ClassPathResource(STUB_FIXTURE_PATH)
        return resource.inputStream.use { stream ->
            objectMapper.readValue(stream, NaverLocalSearchResponse::class.java)
        }
    }

    companion object {
        private const val STUB_FIXTURE_PATH = "fixtures/naver/local-search-cafe.json"
    }
}
