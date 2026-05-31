# Cafe Harness — 대한민국 카페 소개

**Spring Boot 4 + Kotlin 2.2 + Thymeleaf + MySQL** 기반의 카페 소개 웹.
"Harness Engineering" 철학에 따라 **결정론적 규칙을 코드로 강제**하고,
AI 코딩 에이전트가 일관된 품질의 코드를 생산할 수 있도록 환경을 구축한다.

## Stack

- **Language**: Kotlin 2.2.21 / JVM 17
- **Framework**: Spring Boot 4.0.6 (web-mvc, data-jpa, thymeleaf, validation, actuator)
- **DB**: MySQL 8.4 (Docker Compose) — 테스트는 H2(MySQL 호환 모드)
- **Build**: Gradle 9 (Kotlin DSL)
- **Quality gates**: ktlint + detekt + ArchUnit

## Harness — 결정론적 게이트

| 도구 | 강제 대상 | 명령 |
|---|---|---|
| **ktlint** | 포맷팅 + 기본 스타일 | `./gradlew ktlintCheck` / 자동수정: `./gradlew ktlintFormat` |
| **ArchUnit** | 레이어 경계 | `./gradlew test --tests "*architecture*"` |
| **JUnit5** | 단위 테스트 | `./gradlew test` |
| **`./gradlew build`** | 위 셋 모두 | merge gate |

> **detekt**: 1.23.8이 Kotlin 2.0.x까지만 지원하고 2.0.x 미릴리즈 상태라 *일시 비활성화*. `detekt.yml`은 남겨둠. detekt 2.x 릴리즈 시 `build.gradle.kts` 상단 주석 해제 + `check`에 다시 의존성 추가.

**원칙**: 새 코드가 위 게이트를 통과하지 못하면 머지 금지. 게이트를 우회하지 말고 코드를 고친다.

## Architecture — 패키지 경계

```
com.harness.cafe/
├── CafeApplication.kt
├── domain/              # 엔티티, Repository 인터페이스, 도메인 타입(Region)
├── application/         # 유스케이스 (Service)
├── infrastructure/      # 어댑터, 시드 데이터, 외부 시스템 통합
└── interfaces/          # 컨트롤러 (HTTP/Thymeleaf)
```

**의존성 방향** (ArchUnit이 강제):
- `domain` ← `application` ← `interfaces`
- `domain` ← `infrastructure`
- **금지**: `domain → 어디든`, `application → infrastructure/interfaces`, `infrastructure → interfaces`

새 도메인 추가 시 같은 4-레이어 구조 반복.

## 실행

```bash
# DB 띄우기 (최초 1회 / 컨테이너 중지 후)
docker compose up -d

# 앱 실행
./gradlew bootRun

# → http://localhost:8080/cafes
```

## 검증

```bash
./gradlew build         # 전체 게이트 (ktlint + detekt + test + ArchUnit)
./gradlew ktlintFormat  # 포맷 자동 수정
./gradlew test          # 단위 + 아키텍처 테스트만
```

## Code style

- 주석: *왜*가 비자명할 때만. *무엇*은 식별자가 설명.
- 추측성 추상화 금지. 비슷한 3줄 < 잘못된 추상화.
- 에러 핸들링은 시스템 경계(HTTP/DB)에서만. 내부 신뢰.
- 변경 범위 = 요청 범위. 리팩토링 끼워팔지 않는다.
- Kotlin: `data class`는 값 객체에만. JPA `@Entity`는 일반 `class` + `val` 프로퍼티.
- 트랜잭션: Service에 `@Transactional`. 읽기 전용은 `readOnly = true`.

## 오케스트레이션

메인 세션은 코디네이터. 위임 규칙은 `.claude/agents/orchestrator.md` 참조.
주요 위임 대상: `Explore` (검색), `Plan` (설계), `implementer` (구현), `reviewer` (리뷰), `tester` (gradle 실행).

## Git

- 메인 브랜치: `main`
- 커밋은 사용자가 명시적으로 요청할 때만
- 새 작업은 가능하면 feature branch (`feat/...`, `fix/...`)
