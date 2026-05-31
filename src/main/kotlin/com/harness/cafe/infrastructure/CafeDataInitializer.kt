package com.harness.cafe.infrastructure

import com.harness.cafe.domain.Cafe
import com.harness.cafe.domain.CafeRepository
import com.harness.cafe.domain.Region
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
@Profile("!test")
class CafeDataInitializer(
    private val cafeRepository: CafeRepository,
) : CommandLineRunner {
    override fun run(vararg args: String) {
        if (cafeRepository.count() > 0) return
        cafeRepository.saveAll(seedCafes())
    }

    private fun seedCafes(): List<Cafe> =
        listOf(
            Cafe(
                name = "블루보틀 성수",
                region = Region.SEOUL,
                address = "서울특별시 성동구 아차산로 7",
                description = "성수동의 대표 스페셜티 커피 카페. 미니멀한 인테리어와 정성스러운 핸드드립으로 유명합니다.",
                imageUrl = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800",
                signatureMenu = "뉴올리언스 라떼",
            ),
            Cafe(
                name = "어니언 안국",
                region = Region.SEOUL,
                address = "서울특별시 종로구 계동길 5",
                description = "한옥을 개조한 베이커리 카페. 갓 구운 빵과 함께 즐기는 커피 한 잔이 일품입니다.",
                imageUrl = "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800",
                signatureMenu = "팡도르",
            ),
            Cafe(
                name = "프릳츠 도화점",
                region = Region.SEOUL,
                address = "서울특별시 마포구 새창로 2길 17",
                description = "물개 로고로 유명한 로스터리. 직접 로스팅한 스페셜티 원두로 깊은 풍미를 자랑합니다.",
                imageUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800",
                signatureMenu = "에티오피아 예가체프",
            ),
            Cafe(
                name = "모모스 커피 본점",
                region = Region.BUSAN,
                address = "부산광역시 금정구 오시리아로 7",
                description = "월드 바리스타 챔피언이 이끄는 부산 대표 스페셜티 카페. 다양한 싱글 오리진을 만나볼 수 있습니다.",
                imageUrl = "https://images.unsplash.com/photo-1442512595331-e89e73853f31?w=800",
                signatureMenu = "콜드 브루",
            ),
            Cafe(
                name = "베르크 로스터스",
                region = Region.BUSAN,
                address = "부산광역시 해운대구 송정중앙로 6번길 18",
                description = "송정 바닷가 근처의 로스터리. 파도 소리를 들으며 즐기는 한 잔의 여유.",
                imageUrl = "https://images.unsplash.com/photo-1453614512568-c4024d13c247?w=800",
                signatureMenu = "플랫 화이트",
            ),
            Cafe(
                name = "테라로사 강릉 본점",
                region = Region.GANGWON,
                address = "강원특별자치도 강릉시 구정면 현천길 7",
                description = "강릉 커피의 자존심. 넓은 정원과 함께하는 로스터리 카페로 전국에서 손님이 찾아옵니다.",
                imageUrl = "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800",
                signatureMenu = "에스프레소",
            ),
            Cafe(
                name = "보헴 제주",
                region = Region.JEJU,
                address = "제주특별자치도 제주시 한림읍 한림로 542",
                description = "협재 해변이 한눈에 보이는 오션뷰 카페. 제주 감성을 가득 담은 디저트로 유명합니다.",
                imageUrl = "https://images.unsplash.com/photo-1521017432531-fbd92d768814?w=800",
                signatureMenu = "한라봉 에이드",
            ),
            Cafe(
                name = "앤트러사이트 서교",
                region = Region.SEOUL,
                address = "서울특별시 마포구 서교동 357-6",
                description = "신발 공장을 개조한 인더스트리얼 감성 카페. 묵직한 분위기에서 즐기는 진한 커피.",
                imageUrl = "https://images.unsplash.com/photo-1559496417-e7f25cb247f3?w=800",
                signatureMenu = "더치 커피",
            ),
        )
}
