package com.harness.cafe.application

import com.harness.cafe.domain.Cafe
import com.harness.cafe.domain.CafeRepository
import com.harness.cafe.domain.CafeSource
import com.harness.cafe.domain.Region
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@ActiveProfiles("test")
@Import(CafeImportServiceTestConfig::class)
class CafeImportServiceTest {
    @Autowired
    private lateinit var service: CafeImportService

    @Autowired
    private lateinit var cafeRepository: CafeRepository

    @Autowired
    private lateinit var fakeClient: FakeNaverLocalSearchClient

    @BeforeEach
    fun cleanDb() {
        cafeRepository.deleteAll()
        fakeClient.responseToReturn = NaverLocalSearchResponse()
    }

    @Test
    fun `imports cafe items, strips title HTML, populates external fields`() {
        fakeClient.responseToReturn =
            NaverLocalSearchResponse(
                total = 1,
                start = 1,
                display = 1,
                items =
                    listOf(
                        NaverLocalSearchItem(
                            title = "<b>블루보틀</b> 강남",
                            link = "https://naver.example/cafe/seoul-1",
                            category = "음식점>카페,디저트>커피전문점",
                            description = "강남 <b>카페</b>",
                            telephone = "02-9999-0000",
                            address = "서울 강남구 테헤란로 9",
                            roadAddress = "서울특별시 강남구 테헤란로 9",
                            mapX = 1270000001,
                            mapY = 374000002,
                        ),
                    ),
            )

        val result = service.importFromNaver("강남 카페")

        assertEquals(1, result.imported)
        assertEquals(0, result.duplicate)
        assertEquals(0, result.nonCafe)
        assertEquals(0, result.regionUnknown)
        val saved = cafeRepository.findByExternalSourceAndExternalId(CafeSource.NAVER, "https://naver.example/cafe/seoul-1")
        assertNotNull(saved)
        assertEquals("블루보틀 강남", saved.name)
        assertEquals(Region.SEOUL, saved.region)
        assertEquals(CafeSource.NAVER, saved.externalSource)
        assertEquals("https://naver.example/cafe/seoul-1", saved.externalId)
        assertEquals("https://naver.example/cafe/seoul-1", saved.externalUrl)
        assertEquals("02-9999-0000", saved.phone)
        assertEquals(1270000001L, saved.mapX)
        assertEquals(374000002L, saved.mapY)
    }

    @Test
    fun `skips non-cafe categories`() {
        fakeClient.responseToReturn =
            NaverLocalSearchResponse(
                items =
                    listOf(
                        NaverLocalSearchItem(
                            title = "한식당",
                            link = "https://naver.example/restaurant/1",
                            category = "음식점>한식>국밥",
                            description = "",
                            telephone = "",
                            address = "서울 강남구 역삼로 1",
                            roadAddress = "서울특별시 강남구 역삼로 1",
                            mapX = 0,
                            mapY = 0,
                        ),
                    ),
            )

        val result = service.importFromNaver("강남 음식")

        assertEquals(0, result.imported)
        assertEquals(1, result.nonCafe)
        assertEquals(0, cafeRepository.count())
    }

    @Test
    fun `skips items with unknown region`() {
        fakeClient.responseToReturn =
            NaverLocalSearchResponse(
                items =
                    listOf(
                        NaverLocalSearchItem(
                            title = "외계 카페",
                            link = "https://naver.example/cafe/alien-1",
                            category = "음식점>카페,디저트>커피전문점",
                            description = "",
                            telephone = "",
                            address = "안드로메다 알파성 1",
                            roadAddress = "안드로메다 알파성 1",
                            mapX = 0,
                            mapY = 0,
                        ),
                    ),
            )

        val result = service.importFromNaver("외계 카페")

        assertEquals(0, result.imported)
        assertEquals(1, result.regionUnknown)
        assertNull(
            cafeRepository.findByExternalSourceAndExternalId(CafeSource.NAVER, "https://naver.example/cafe/alien-1"),
        )
    }

    @Test
    fun `skips duplicates by external source and external id`() {
        cafeRepository.save(
            Cafe(
                name = "이미 있는 카페",
                region = Region.BUSAN,
                address = "부산광역시 해운대구 구남로 1",
                description = "기존",
                externalSource = CafeSource.NAVER,
                externalId = "https://naver.example/cafe/dup-1",
                externalUrl = "https://naver.example/cafe/dup-1",
            ),
        )
        fakeClient.responseToReturn =
            NaverLocalSearchResponse(
                items =
                    listOf(
                        NaverLocalSearchItem(
                            title = "<b>이미 있는</b> 카페",
                            link = "https://naver.example/cafe/dup-1",
                            category = "음식점>카페,디저트>커피전문점",
                            description = "",
                            telephone = "051-0000-0000",
                            address = "부산 해운대구 구남로 1",
                            roadAddress = "부산광역시 해운대구 구남로 1",
                            mapX = 1290000000,
                            mapY = 350000000,
                        ),
                    ),
            )

        val result = service.importFromNaver("해운대 카페")

        assertEquals(0, result.imported)
        assertEquals(1, result.duplicate)
        assertEquals(1, cafeRepository.count())
    }
}

@TestConfiguration
class CafeImportServiceTestConfig {
    @Bean
    @Primary
    fun fakeNaverLocalSearchClient(): FakeNaverLocalSearchClient = FakeNaverLocalSearchClient()
}

class FakeNaverLocalSearchClient : NaverLocalSearchClient {
    var responseToReturn: NaverLocalSearchResponse = NaverLocalSearchResponse()

    override fun search(query: String): NaverLocalSearchResponse = responseToReturn
}
