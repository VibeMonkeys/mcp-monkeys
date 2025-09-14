# 서비스 분리 제안서

## 🤔 현재 문제점

### 현재 ChatService의 책임
```kotlin
@Service
class ChatService {
    fun generateChatResponse()      // 1. 일반 텍스트 채팅
    fun generateStructuredResponse() // 2. 구조화된 응답 생성
    fun generateStreamingResponse()  // 3. 스트리밍 응답 생성
    // + 컨텍스트 관리, 메트릭 수집, 예외 처리...
}
```

**문제점:**
- **단일 책임 원칙(SRP) 위반**: 3가지 다른 응답 방식 + 여러 횡단 관심사
- **응집도 낮음**: 서로 다른 기술적 구현 (동기/비동기, 타입 안전성)
- **테스트 복잡성**: 하나의 클래스에서 너무 많은 것을 테스트
- **확장성 제한**: 새로운 응답 방식 추가 시 기존 코드 영향

## 💡 제안하는 분리 구조

### 1️⃣ 응답 타입별 서비스 분리

```kotlin
// 1. 일반 채팅 서비스
@Service
class BasicChatService(
    private val chatClient: ChatClient,
    private val conversationMemoryService: ConversationMemoryService,
    private val aiMetricsService: AiMetricsService
) {
    fun generateResponse(request: ChatRequest): ChatResponse
}

// 2. 구조화된 응답 서비스  
@Service
class StructuredResponseService(
    private val chatClient: ChatClient,
    private val aiMetricsService: AiMetricsService
) {
    fun generateStructuredResponse(request: ChatRequest): StructuredChatResponse
}

// 3. 스트리밍 서비스
@Service  
class StreamingChatService(
    private val chatClient: ChatClient,
    private val aiMetricsService: AiMetricsService
) {
    fun generateStreamingResponse(message: String, sessionId: String?): Flux<String>
}
```

### 2️⃣ 통합 Facade 패턴

```kotlin
@Service
class ChatOrchestrationService(
    private val basicChatService: BasicChatService,
    private val structuredResponseService: StructuredResponseService,
    private val streamingChatService: StreamingChatService
) {
    fun processRequest(request: ChatRequest): ResponseEntity<BaseResponse<Any>> {
        return when (request.format) {
            "structured" -> handleStructured(request)
            "streaming" -> handleStreaming(request)
            else -> handleBasic(request)
        }
    }
    
    private fun handleBasic(request: ChatRequest): ResponseEntity<BaseResponse<Any>> {
        val response = basicChatService.generateResponse(request)
        return ResponseEntity.ok(BaseResponse.success(response))
    }
    
    private fun handleStructured(request: ChatRequest): ResponseEntity<BaseResponse<Any>> {
        val response = structuredResponseService.generateStructuredResponse(request)
        return ResponseEntity.ok(BaseResponse.success(response))
    }
}
```

### 3️⃣ 개선된 컨트롤러

```kotlin
@RestController
class UnifiedChatController(
    private val chatOrchestrationService: ChatOrchestrationService,
    private val streamingChatService: StreamingChatService
) {
    @PostMapping("/chat")
    fun chat(@RequestBody request: ChatRequest): ResponseEntity<BaseResponse<Any>> {
        return chatOrchestrationService.processRequest(request)
    }
    
    @GetMapping("/chat/stream")
    fun chatStream(@RequestParam message: String, @RequestParam sessionId: String?): Flux<String> {
        return streamingChatService.generateStreamingResponse(message, sessionId)
    }
}
```

## ✅ 분리의 장점

### 1. **단일 책임 원칙 준수**
- 각 서비스가 하나의 명확한 책임
- BasicChatService: 일반 대화만
- StructuredResponseService: 타입 안전한 구조화된 응답만
- StreamingChatService: 실시간 스트리밍만

### 2. **테스트 용이성**
```kotlin
@Test
class BasicChatServiceTest {
    // 일반 채팅 로직만 테스트
    fun `should generate basic chat response`() { }
}

@Test  
class StructuredResponseServiceTest {
    // 구조화된 응답 로직만 테스트
    fun `should parse structured response correctly`() { }
}
```

### 3. **확장성 향상**
```kotlin
// 새로운 응답 방식 추가 시
@Service
class AudioResponseService { } // 기존 코드 영향 없음

// ChatOrchestrationService에만 추가
when (request.format) {
    "audio" -> handleAudio(request)
    // ...
}
```

### 4. **성능 최적화**
```kotlin
@Service
class StreamingChatService {
    // 스트리밍에 특화된 설정
    @Value("\${ai.streaming.chunk-size:1024}")
    private val chunkSize: Int
    
    // 비동기 처리에 최적화된 ThreadPool
    private val streamingExecutor = Executors.newVirtualThreadPerTaskExecutor()
}
```

## 🚨 분리의 단점과 해결책

### 1. **코드 중복**
**문제**: 공통 로직(메트릭, 예외 처리) 중복
**해결**: 공통 기능을 별도 컴포넌트로 분리
```kotlin
@Component
class ChatMetricsCollector {
    fun <T> withMetrics(operation: () -> T): T
}

@Component  
class ChatExceptionHandler {
    fun <T> withErrorHandling(operation: () -> T): T
}
```

### 2. **복잡성 증가**
**문제**: 클래스 수 증가
**해결**: 패키지 구조로 정리
```
service/
├── chat/
│   ├── BasicChatService.kt
│   ├── StructuredResponseService.kt
│   └── StreamingChatService.kt
├── orchestration/
│   └── ChatOrchestrationService.kt
└── common/
    ├── ChatMetricsCollector.kt
    └── ChatExceptionHandler.kt
```

## 🎯 권장사항

### **점진적 분리 전략**

#### Phase 1: 공통 기능 추출
```kotlin
@Component
class ChatResponseHelper {
    fun buildPrompt(message: String, context: String): String
    fun estimateTokenCount(text: String): Int
    fun maskMessage(message: String): String
}
```

#### Phase 2: 스트리밍 서비스 분리
```kotlin
// 가장 독립적이므로 먼저 분리
@Service
class StreamingChatService { }
```

#### Phase 3: 나머지 응답 타입 분리
```kotlin
@Service  
class BasicChatService { }

@Service
class StructuredResponseService { }
```

#### Phase 4: Orchestration 레이어 추가
```kotlin
@Service
class ChatOrchestrationService { }
```

## 🔄 마이그레이션 계획

### 1주차: 공통 기능 추출
- ChatResponseHelper 생성
- 기존 ChatService에서 공통 메서드 이동

### 2주차: StreamingChatService 분리  
- 스트리밍 로직을 별도 서비스로 분리
- 기존 코드와 병행 운영으로 안정성 확인

### 3주차: Basic/Structured 서비스 분리
- 나머지 두 응답 타입 분리
- 테스트 코드 작성 및 검증

### 4주차: Orchestration 레이어 추가
- ChatOrchestrationService 구현
- 컨트롤러 리팩터링

## 🎯 결론

**분리를 권장합니다!** 

현재 ChatService는 확실히 **단일 책임 원칙**을 위반하고 있으며, 각 응답 방식이 서로 다른 기술적 특성을 가지고 있어 분리의 이점이 클 것으로 예상됩니다.

특히:
- **BasicChatService**: 대화 메모리와 컨텍스트에 집중
- **StructuredResponseService**: 타입 안전성과 파싱에 집중  
- **StreamingChatService**: 비동기 스트리밍 성능에 집중

각자의 전문 영역에서 최적화할 수 있어 전체적인 코드 품질과 성능이 향상될 것입니다! 🚀