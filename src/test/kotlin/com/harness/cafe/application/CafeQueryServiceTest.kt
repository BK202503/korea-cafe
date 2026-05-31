package com.harness.cafe.application

import com.harness.cafe.domain.Cafe
import com.harness.cafe.domain.CafeRepository
import com.harness.cafe.domain.Region
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
@ActiveProfiles("test")
class CafeQueryServiceTest
    @Autowired
    constructor(
        private val service: CafeQueryService,
        private val cafeRepository: CafeRepository,
    ) {
        @BeforeEach
        fun seed() {
            cafeRepository.deleteAll()
            cafeRepository.saveAll(
                listOf(
                    Cafe(name = "서울 카페1", region = Region.SEOUL, address = "서울 주소1", description = "d"),
                    Cafe(name = "서울 카페2", region = Region.SEOUL, address = "서울 주소2", description = "d"),
                    Cafe(name = "부산 카페", region = Region.BUSAN, address = "부산 주소", description = "d"),
                ),
            )
        }

        @Test
        fun `findAll 은 저장된 모든 카페를 반환한다`() {
            assertEquals(3, service.findAll().size)
        }

        @Test
        fun `findByRegion 은 해당 지역 카페만 반환한다`() {
            val busan = service.findByRegion(Region.BUSAN)
            assertEquals(1, busan.size)
            assertEquals("부산 카페", busan.first().name)
        }

        @Test
        fun `findByRegion 은 카페가 없는 지역에 대해 빈 리스트를 반환한다`() {
            assertTrue(service.findByRegion(Region.JEJU).isEmpty())
        }

        @Test
        fun `findById 는 존재하지 않는 id 에 대해 CafeNotFoundException 을 던진다`() {
            val ex = assertThrows<CafeNotFoundException> { service.findById(999L) }
            assertTrue(ex.message!!.contains("999"))
        }

        @Test
        fun `findById 는 존재하는 카페를 반환한다`() {
            val saved = cafeRepository.findAll().first()
            val found = service.findById(saved.id!!)
            assertEquals(saved.name, found.name)
        }
    }
