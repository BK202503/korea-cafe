package com.harness.cafe.application

import com.harness.cafe.domain.Cafe
import com.harness.cafe.domain.CafeRepository
import com.harness.cafe.domain.CafeSource
import com.harness.cafe.domain.Region
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CafeImportService(
    private val cafeRepository: CafeRepository,
    private val naverClient: NaverLocalSearchClient,
) {
    fun importFromNaver(query: String): ImportResult {
        val response = naverClient.search(query)
        var imported = 0
        var dup = 0
        var nonCafe = 0
        var regionUnknown = 0
        for (item in response.items) {
            if (!isCafeCategory(item.category)) {
                nonCafe++
                continue
            }
            val region = resolveRegion(item.roadAddress)
            if (region == null) {
                regionUnknown++
                continue
            }
            val existing = cafeRepository.findByExternalSourceAndExternalId(CafeSource.NAVER, item.link)
            if (existing != null) {
                dup++
                continue
            }
            cafeRepository.save(
                Cafe(
                    name = stripHtml(item.title),
                    region = region,
                    address = item.roadAddress.ifBlank { item.address },
                    description = stripHtml(item.description).ifBlank { stripHtml(item.title) },
                    externalSource = CafeSource.NAVER,
                    externalId = item.link,
                    externalUrl = item.link,
                    mapX = item.mapX,
                    mapY = item.mapY,
                    phone = item.telephone.ifBlank { null },
                ),
            )
            imported++
        }
        return ImportResult(imported = imported, duplicate = dup, nonCafe = nonCafe, regionUnknown = regionUnknown)
    }

    private fun isCafeCategory(category: String): Boolean = category.contains("카페")

    private fun stripHtml(value: String): String = HTML_TAG_REGEX.replace(value, "")

    private fun resolveRegion(roadAddress: String): Region? {
        val firstToken =
            roadAddress
                .trim()
                .split(' ')
                .firstOrNull()
                .orEmpty()
        return REGION_BY_PREFIX[firstToken]
    }

    companion object {
        private val HTML_TAG_REGEX = Regex("<[^>]+>")
        private val REGION_BY_PREFIX: Map<String, Region> =
            mapOf(
                "서울특별시" to Region.SEOUL,
                "부산광역시" to Region.BUSAN,
                "인천광역시" to Region.INCHEON,
                "대구광역시" to Region.DAEGU,
                "대전광역시" to Region.DAEJEON,
                "광주광역시" to Region.GWANGJU,
                "울산광역시" to Region.ULSAN,
                "경기도" to Region.GYEONGGI,
                "강원특별자치도" to Region.GANGWON,
                "강원도" to Region.GANGWON,
                "제주특별자치도" to Region.JEJU,
            )
    }
}

data class ImportResult(
    val imported: Int,
    val duplicate: Int,
    val nonCafe: Int,
    val regionUnknown: Int,
)
