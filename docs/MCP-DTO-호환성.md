# MCP DTO 호환성 가이드

## 개요

MCP (Model Context Protocol) 아키텍처에서 Client와 Server 간의 데이터 교환을 위한 DTO(Data Transfer Object) 호환성에 대한 가이드입니다.

## 핵심 원칙

### ✅ **MCP Client ↔ MCP Server: 반드시 동일한 DTO 사용**

```
MCP Weather Server  ←→  WeatherInfo DTO  ←→  MCP Client
MCP News Server     ←→  NewsArticle DTO  ←→  MCP Client  
MCP Translate Server ←→ TranslationResult DTO ←→ MCP Client
```

## 아키텍처 구조

### 1. Shared Module 기반 설계

```
shared/src/main/kotlin/com/monkeys/shared/dto/
├── WeatherDto.kt      # WeatherInfo, WeatherForecast, WeatherRequest
├── NewsDto.kt         # NewsArticle, NewsHeadlinesRequest, NewsSearchRequest
├── TranslateDto.kt    # TranslationResult, TranslationRequest, LanguageDetectionResult
├── CalendarDto.kt     # CalendarEvent, CalendarRequest
└── CommonDto.kt       # 공통 응답 형식
```

### 2. 의존성 관계

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  MCP Servers    │────│  Shared Module  │────│   MCP Client    │
│ (Weather, News, │    │   (DTO 정의)    │    │  (통합 처리)    │
│ Translate, etc) │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## 실제 예시

### WeatherInfo DTO 구조

```kotlin
// shared/dto/WeatherDto.kt
data class WeatherInfo(
    @JsonPropertyDescription("도시명")
    val city: String,
    
    @JsonPropertyDescription("국가 코드")  
    val country: String,
    
    @JsonPropertyDescription("현재 온도")
    val temperature: Double,
    
    @JsonPropertyDescription("체감 온도")
    val feelsLike: Double,
    
    @JsonPropertyDescription("습도 (%)")
    val humidity: Int,
    
    @JsonPropertyDescription("기압 (hPa)")
    val pressure: Int,
    
    @JsonPropertyDescription("날씨 설명")
    val description: String,
    
    @JsonPropertyDescription("날씨 주요 상태")
    val main: String,
    
    @JsonPropertyDescription("풍속 (m/s)")
    val windSpeed: Double,
    
    @JsonPropertyDescription("풍향 (degrees)")
    val windDirection: Int,
    
    @JsonPropertyDescription("가시거리 (m)")
    val visibility: Int,
    
    @JsonPropertyDescription("단위 시스템")
    val units: String
)
```

### 데이터 흐름

```
1. 사용자 요청: "서울 날씨 알려줘"
   ↓
2. MCP Client: LLM이 의도 분석 → WeatherInfo 도구 선택
   ↓  
3. MCP Weather Server: MockWeatherRepository.getCurrentWeather() 호출
   ↓
4. Weather Server → WeatherInfo 객체 생성 → JSON 직렬화
   ↓
5. MCP Client ← JSON 응답 수신 → WeatherInfo 객체로 역직렬화
   ↓
6. 사용자 응답: WeatherResponse 형태로 변환하여 최종 응답
```

## Mock Repository 구현

### 1. Profile 기반 Repository 선택

```kotlin
@Repository
@Profile("!external-api")  // 기본값: Mock Repository 사용
class MockWeatherRepository : WeatherRepository {
    // 하드코딩된 데이터로 WeatherInfo 반환
}

@Repository  
@Profile("external-api")   // 외부 API 사용시: External Repository 사용
class OpenWeatherMapRepository : WeatherRepository {
    // 실제 API 호출로 WeatherInfo 반환
}
```

### 2. 동일한 DTO 반환 보장

```kotlin
// Mock Repository
override suspend fun getCurrentWeather(city: String, units: String): WeatherInfo {
    // 하드코딩된 데이터를 WeatherInfo 형태로 반환
    return mockWeatherData[city] ?: defaultWeatherInfo
}

// External API Repository  
override suspend fun getCurrentWeather(city: String, units: String): WeatherInfo {
    // 외부 API 응답을 WeatherInfo 형태로 변환하여 반환
    return mapToWeatherInfo(apiResponse, units)
}
```

## 호환성 검증

### ✅ 올바른 구현

```kotlin
// Server에서 WeatherInfo 반환
fun getCurrentWeather(): WeatherInfo = WeatherInfo(...)

// Client에서 WeatherInfo 수신  
val weatherInfo: WeatherInfo = mcpResponse.parseAs<WeatherInfo>()
```

### ❌ 잘못된 구현 

```kotlin
// Server에서 다른 형태 반환 (호환성 문제)
fun getCurrentWeather(): Map<String, Any> = mapOf(...)

// Client에서 다른 DTO 사용 (파싱 실패)
val weather: SomeOtherDto = mcpResponse.parseAs<SomeOtherDto>()
```

## JSON 직렬화/역직렬화

### Jackson 어노테이션 활용

```kotlin
data class WeatherInfo(
    @JsonPropertyDescription("도시명")
    val city: String,
    
    @JsonProperty("temperature") 
    val temperature: Double,
    
    // @JsonPropertyDescription은 Spring AI가 도구 설명에 활용
)
```

### 직렬화 과정

```
MCP Server: WeatherInfo 객체 → Jackson → JSON 문자열 → HTTP 응답
     ↓
MCP Client: HTTP 수신 → JSON 파싱 → Jackson → WeatherInfo 객체
```

## 버전 관리

### Shared Module 버전 동기화

```gradle
// 모든 서버와 클라이언트가 동일한 shared 버전 사용
dependencies {
    implementation project(':shared')  // 항상 동일한 버전
}
```

### DTO 변경시 주의사항

1. **Breaking Change 방지**: 기존 필드 삭제 금지
2. **Optional Field 추가**: 새 필드는 기본값 제공
3. **동시 배포**: 모든 서버와 클라이언트 동시 업데이트

## 트러블슈팅

### 자주 발생하는 문제

1. **필드명 불일치**
   ```kotlin
   // 문제: Server에서 cityName, Client에서 city 기대
   // 해결: shared 모듈의 정확한 필드명 사용
   ```

2. **타입 불일치**  
   ```kotlin
   // 문제: Server에서 String, Client에서 Int 기대
   // 해결: DTO 정의를 정확히 따름
   ```

3. **Null 값 처리**
   ```kotlin
   // 문제: Non-null 필드에 null 전송
   // 해결: Optional 필드 사용 또는 기본값 제공
   ```

### 디버깅 방법

```bash
# 1. 서버 로그에서 반환 데이터 확인
tail -f mcp-weather-server.log | grep "응답"

# 2. 클라이언트 로그에서 수신 데이터 확인  
tail -f mcp-client.log | grep "MCP 응답"

# 3. JSON 구조 비교
curl http://localhost:8092/weather/seoul | jq .
```

## 결론

- **핵심**: MCP Client와 Server는 반드시 동일한 DTO 사용
- **구현**: Shared module을 통한 DTO 공유로 호환성 보장
- **장점**: 타입 안정성, 컴파일 타임 검증, 코드 재사용
- **주의**: DTO 변경시 모든 컴포넌트 동시 업데이트 필요

Mock Repository 도입으로 외부 API 의존성 없이도 동일한 DTO 구조를 유지하며 데모 환경을 구축할 수 있습니다.