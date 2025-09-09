package com.monkeys.client.controller

import com.monkeys.client.config.ConfigurationValidator
import com.monkeys.client.config.McpServerUrls
import com.monkeys.shared.circuit.SimpleCircuitBreakerRegistry
import com.monkeys.shared.circuit.CircuitBreakerOpenException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono
import reactor.core.publisher.Mono.just as toMono
import org.slf4j.LoggerFactory
import java.time.Duration
import io.micrometer.core.instrument.MeterRegistry

@RestController
@RequestMapping("/api/health")
@CrossOrigin(origins = ["http://localhost:3004", "http://localhost:3000"])
class HealthController(
    private val configurationValidator: ConfigurationValidator,
    private val mcpServerUrls: McpServerUrls,
    private val webClient: WebClient.Builder,
    private val circuitBreakerRegistry: SimpleCircuitBreakerRegistry,
    private val meterRegistry: MeterRegistry
) {
    
    private val logger = LoggerFactory.getLogger(HealthController::class.java)
    
    @GetMapping("/config")
    fun getConfigurationStatus(): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.ok(
            mapOf(
                "apiCredentials" to configurationValidator.validateApiConfiguration(),
                "instructions" to configurationValidator.getConfigurationInstructions(),
                "timestamp" to System.currentTimeMillis()
            )
        )
    }
    
    @GetMapping("/mcp-servers")
    fun checkMcpServers(): Mono<ResponseEntity<Map<String, Any>>> {
        val serverChecks = mapOf(
            "weather" to mcpServerUrls.weather.url,
            "news" to mcpServerUrls.news.url, 
            "translate" to mcpServerUrls.translate.url,
            "calendar" to mcpServerUrls.calendar.url
        )
        
        val client = webClient.build()
        val checks = serverChecks.map { (name, url) ->
            checkServerHealth(client, name, url)
                .map { name to it }
        }
        
        return Mono.zip(checks) { results ->
            @Suppress("UNCHECKED_CAST")
            val statusMap = results.filterIsInstance<Pair<String, Map<String, String>>>().toMap() as Map<String, Map<String, Any>>
            
            ResponseEntity.ok(
                mapOf(
                    "servers" to statusMap,
                    "overallStatus" to if (statusMap.values.all { it["status"] == "UP" }) "UP" else "DEGRADED",
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }
    
    @GetMapping("/comprehensive")
    fun getComprehensiveHealth(): Mono<ResponseEntity<Map<String, Any>>> {
        return checkMcpServers().map { mcpResponse ->
            val mcpData = mcpResponse.body ?: emptyMap<String, Any>()
            
            ResponseEntity.ok(
                mapOf(
                    "service" to mapOf(
                        "name" to "unified-mcp-client",
                        "version" to "1.0.0", 
                        "status" to "UP"
                    ),
                    "apiCredentials" to configurationValidator.validateApiConfiguration(),
                    "mcpServers" to (mcpData["servers"] ?: emptyMap<String, Any>()),
                    "overallHealth" to if (mcpData["overallStatus"] == "UP") "HEALTHY" else "DEGRADED",
                    "configurationInstructions" to configurationValidator.getConfigurationInstructions(),
                    "timestamp" to System.currentTimeMillis()
                )
            )
        }
    }
    
    private fun checkServerHealth(client: WebClient, name: String, url: String): Mono<Map<String, String>> {
        val circuitBreaker = circuitBreakerRegistry.getCircuitBreaker("mcp-$name")
        val startTime = System.nanoTime()
        
        return try {
            // Circuit Breaker로 감싸진 비동기 호출
            Mono.fromCallable {
                circuitBreaker.execute {
                    client.get()
                        .uri("$url/actuator/health")
                        .retrieve()
                        .bodyToMono(String::class.java)
                        .timeout(Duration.ofSeconds(3))
                        .block() // Circuit Breaker 내에서 동기 호출로 변환
                }
            }
            .map { 
                val responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis()
                meterRegistry.timer("mcp.health.check", "server", name).record(responseTime, java.util.concurrent.TimeUnit.MILLISECONDS)
                
                mapOf(
                    "status" to "UP",
                    "url" to url,
                    "responseTime" to "${responseTime}ms",
                    "circuitBreakerState" to circuitBreaker.getState().name
                )
            }
        } catch (e: CircuitBreakerOpenException) {
            logger.warn("MCP 서버 $name Circuit Breaker OPEN")
            meterRegistry.counter("mcp.health.check.circuit_breaker_open", "server", name).increment()
            
            toMono(mapOf(
                "status" to "DOWN",
                "url" to url,
                "error" to "Circuit Breaker Open",
                "circuitBreakerState" to "OPEN"
            ))
        }.onErrorResume { error ->
            val responseTime = Duration.ofNanos(System.nanoTime() - startTime).toMillis()
            logger.warn("MCP 서버 $name 연결 실패 (${responseTime}ms): ${error.message}")
            
            val errorInfo = when (error) {
                is WebClientResponseException -> "HTTP ${error.statusCode.value()}"
                else -> "Connection Failed"
            }
            
            meterRegistry.counter("mcp.health.check.error", "server", name, "type", errorInfo).increment()
            
            toMono(mapOf(
                "status" to "DOWN",
                "url" to url,
                "error" to errorInfo,
                "responseTime" to "${responseTime}ms",
                "circuitBreakerState" to circuitBreaker.getState().name
            ))
        }
    }
}