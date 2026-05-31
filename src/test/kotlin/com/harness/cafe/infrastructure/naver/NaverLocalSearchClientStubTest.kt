package com.harness.cafe.infrastructure.naver

import com.harness.cafe.application.NaverLocalSearchClient
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class NaverLocalSearchClientStubTest {
    @Autowired
    private lateinit var client: NaverLocalSearchClient

    @Test
    fun `stub mode returns fixture when client-id is blank`() {
        val response = client.search("강남 카페")

        assertEquals(5, response.items.size)
        assertTrue(response.items.any { it.category.contains("카페") })
        assertTrue(response.items.any { !it.category.contains("카페") })
        assertTrue(response.items.any { it.roadAddress.startsWith("서울특별시") })
        assertTrue(response.items.any { it.roadAddress.startsWith("부산광역시") })
        assertTrue(response.items.any { it.roadAddress.startsWith("안드로메다") })
    }
}
