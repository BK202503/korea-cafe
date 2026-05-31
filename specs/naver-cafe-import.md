# Spec — Naver Local Search 카페 import

## 목표
네이버 지역검색 Open API를 호출해 카페 정보를 가져와 `Cafe` 도메인에 저장한다.
실제 운영용 데이터 소스 추가가 아니라, **외부 API 연동 패턴** 을 harness 위에서 검증하는 것이 1차 목표.

## In scope
- HTTP 클라이언트 (`infrastructure/naver/`)
- 응답을 `Cafe` 도메인으로 매핑 (HTML 태그 제거, 카테고리 필터)
- import 트리거 — CLI 인자(`--import-naver=강남 카페,부산 해운대 카페`)로 `ApplicationRunner` 동작
- 중복 방지 — `externalSource + externalId` 유니크
- **stub 모드**: 환경변수 `NAVER_CLIENT_ID` / `NAVER_CLIENT_SECRET` 미설정 시 fixture JSON 사용 (테스트 + 로컬 데모용)
- 단위 테스트 + ArchUnit 검증

## Out of scope
- 좌표계 변환 (KATEC TM128 → WGS84). `mapX`, `mapY` 정수 그대로 저장만.
- 실시간/주기 동기화
- 양방향 sync, 갱신, 삭제 전파

## 외부 API 사양
- Endpoint: `GET https://openapi.naver.com/v1/search/local.json`
- Headers: `X-Naver-Client-Id`, `X-Naver-Client-Secret`
- Query
  - `query` (필수) — 검색어 (예: `강남 카페`)
  - `display` (1~5) — 결과 개수
  - `start` (1) — 페이지네이션 거의 무의미
  - `sort` (`random` | `comment`)
- 응답 item 필드 (사용할 것만): `title` (HTML 태그 포함), `link`, `category`, `description`, `telephone`, `address`, `roadAddress`, `mapx`, `mapy`
- 일 25,000건 제한, 무료

## 데이터 모델 변경
`com.harness.cafe.domain.Cafe` 에 필드 추가 (nullable, JPA `ddl-auto: update` 가 자동 마이그레이션):

| 필드 | 타입 | 비고 |
|---|---|---|
| `externalSource` | `enum CafeSource { SEED, NAVER }` | 기본값 `SEED` |
| `externalId` | `String?` | Naver는 `link` 값을 ID로 사용 |
| `externalUrl` | `String?` | Naver `link` |
| `mapX` | `Long?` | KATEC TM128 X |
| `mapY` | `Long?` | KATEC TM128 Y |
| `phone` | `String?` | Naver `telephone` |

추가로 `domain/CafeSource.kt` 생성. `CafeRepository` 에 `findByExternalSourceAndExternalId(source, id): Cafe?` 추가.

기존 시드 데이터(`CafeDataInitializer`)는 `externalSource = SEED` 로 명시 저장하도록 보정.

## Region 매핑
Naver `roadAddress` 의 첫 토큰("서울특별시", "부산광역시" 등)으로 `Region` 추정.
매칭 실패 시 해당 카페는 import 스킵하고 WARN 로그.

```
"서울특별시" → SEOUL, "부산광역시" → BUSAN, "인천광역시" → INCHEON,
"대구광역시" → DAEGU, "대전광역시" → DAEJEON, "광주광역시" → GWANGJU,
"울산광역시" → ULSAN, "경기도" → GYEONGGI, "강원특별자치도" / "강원도" → GANGWON,
"제주특별자치도" → JEJU
```

## 카테고리 필터
응답 `category` 가 "카페" 를 포함하는 항목만 채택 (예: `음식점>카페,디저트>커피전문점`).
나머지는 스킵.

## title HTML 처리
응답 `title` 은 `<b>...</b>` 등 검색어 강조 태그 포함. 정규식 `<[^>]+>` 로 제거 후 사용.

## 파일 배치 (레이어 경계 준수)

```
src/main/kotlin/com/harness/cafe/
├── domain/
│   ├── Cafe.kt                       # 필드 추가
│   ├── CafeRepository.kt             # 메서드 추가
│   └── CafeSource.kt                 # 신규 enum
├── application/
│   └── CafeImportService.kt          # 신규 — 외부 응답 → 도메인 저장
├── infrastructure/
│   ├── CafeDataInitializer.kt        # 기존 시드에 externalSource = SEED 명시
│   ├── CafeImportRunner.kt           # 신규 — ApplicationRunner, CLI 인자 파싱
│   └── naver/
│       ├── NaverLocalSearchClient.kt # 신규 — Spring RestClient 기반 (webmvc 포함)
│       ├── NaverLocalSearchProperties.kt # @ConfigurationProperties("naver.local-search")
│       └── dto/
│           └── NaverLocalSearchResponse.kt # 응답 DTO (data class)
└── interfaces/
    └── (변경 없음 — 외부 API는 interfaces 에 노출 안 함)
```

ArchUnit 규칙은 변경 불필요 — 신규 파일들이 기존 레이어 규칙을 그대로 따른다.

## 환경 설정 (`application.yml`)
```yaml
naver:
  local-search:
    base-url: https://openapi.naver.com
    client-id: ${NAVER_CLIENT_ID:}
    client-secret: ${NAVER_CLIENT_SECRET:}
```

`client-id` 가 비어 있으면 클라이언트는 **stub 모드** — `src/main/resources/fixtures/naver/local-search-cafe.json` 의 고정 응답을 반환.

## CLI 트리거
```bash
./gradlew bootRun --args='--import-naver=강남 카페,해운대 카페'
```
콤마로 분리된 각 쿼리에 대해 client 호출 → 카페 필터 → Region 매핑 → 저장.
이미 동일 `externalSource + externalId` 가 존재하면 스킵.
실행 후 통계 INFO 로그: `imported=N skipped(dup)=N skipped(non-cafe)=N skipped(region-unknown)=N`.

## 테스트

| 위치 | 내용 |
|---|---|
| `infrastructure/naver/NaverLocalSearchClientStubTest.kt` | stub 모드 동작 — env 없을 때 fixture 반환 |
| `application/CafeImportServiceTest.kt` | `@SpringBootTest @ActiveProfiles("test")` + fake client 주입 → 중복/필터/매핑 검증 |
| `architecture/LayerArchitectureTest.kt` | 변경 없이도 통과해야 함 (의존 방향 그대로) |

## Harness 통과 기준
```bash
./gradlew build
```
- ktlint ✓
- ArchUnit ✓ (레이어 규칙 변경 없음)
- 신규 테스트 + 기존 테스트 모두 ✓
- 컴파일 ✓

## Non-goals 명시 재확인
- 좌표 변환 안 함 (그대로 저장만)
- 실 호출 통합 테스트 안 함 (실제 NAVER_CLIENT_ID 없으면 통합 테스트 의미 없음)
- 관리자 UI 안 만듦 (CLI 한 방향으로 충분)
