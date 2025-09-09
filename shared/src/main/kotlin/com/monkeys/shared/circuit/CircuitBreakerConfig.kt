package com.monkeys.shared.circuit

import io.micrometer.core.instrument.MeterRegistry
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

@Configuration
class CircuitBreakerConfig(
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(CircuitBreakerConfig::class.java)

    @Bean
    fun simpleCircuitBreakerRegistry(): SimpleCircuitBreakerRegistry {
        return SimpleCircuitBreakerRegistry(meterRegistry)
    }
}

/**
 * 간단한 Circuit Breaker 구현
 * 실제 프로덕션에서는 Resilience4j 같은 라이브러리 사용 권장
 */
class SimpleCircuitBreakerRegistry(
    private val meterRegistry: MeterRegistry
) {
    private val circuitBreakers = ConcurrentHashMap<String, SimpleCircuitBreaker>()
    private val logger = LoggerFactory.getLogger(SimpleCircuitBreakerRegistry::class.java)

    fun getCircuitBreaker(name: String, config: CircuitBreakerProperties = CircuitBreakerProperties()): SimpleCircuitBreaker {
        return circuitBreakers.computeIfAbsent(name) {
            logger.info("Circuit Breaker 생성: $name")
            SimpleCircuitBreaker(name, config, meterRegistry)
        }
    }
}

data class CircuitBreakerProperties(
    val failureThreshold: Int = 5,
    val openTimeoutDuration: Duration = Duration.ofSeconds(30),
    val halfOpenMaxCalls: Int = 3
)

class SimpleCircuitBreaker(
    private val name: String,
    private val config: CircuitBreakerProperties,
    private val meterRegistry: MeterRegistry
) {
    private val logger = LoggerFactory.getLogger(SimpleCircuitBreaker::class.java)
    
    private val failureCount = AtomicInteger(0)
    private val successCount = AtomicInteger(0)
    private val lastFailureTime = AtomicLong(0)
    
    @Volatile
    private var state = State.CLOSED
    
    enum class State {
        CLOSED, OPEN, HALF_OPEN
    }

    fun <T> execute(operation: () -> T): T {
        when (getCurrentState()) {
            State.OPEN -> {
                meterRegistry.counter("circuit_breaker.rejected", "name", name).increment()
                throw CircuitBreakerOpenException("Circuit breaker is OPEN for $name")
            }
            State.HALF_OPEN -> {
                return executeInHalfOpenState(operation)
            }
            State.CLOSED -> {
                return executeInClosedState(operation)
            }
        }
    }

    private fun getCurrentState(): State {
        if (state == State.OPEN) {
            val now = System.currentTimeMillis()
            if (now - lastFailureTime.get() > config.openTimeoutDuration.toMillis()) {
                logger.info("Circuit Breaker $name: OPEN -> HALF_OPEN")
                state = State.HALF_OPEN
                meterRegistry.counter("circuit_breaker.state_change", "name", name, "to", "HALF_OPEN").increment()
            }
        }
        return state
    }

    private fun <T> executeInClosedState(operation: () -> T): T {
        return try {
            val result = operation()
            onSuccess()
            result
        } catch (e: Exception) {
            onFailure()
            throw e
        }
    }

    private fun <T> executeInHalfOpenState(operation: () -> T): T {
        return try {
            val result = operation()
            onSuccess()
            if (successCount.get() >= config.halfOpenMaxCalls) {
                logger.info("Circuit Breaker $name: HALF_OPEN -> CLOSED")
                state = State.CLOSED
                failureCount.set(0)
                meterRegistry.counter("circuit_breaker.state_change", "name", name, "to", "CLOSED").increment()
            }
            result
        } catch (e: Exception) {
            onFailure()
            logger.warn("Circuit Breaker $name: HALF_OPEN -> OPEN")
            state = State.OPEN
            meterRegistry.counter("circuit_breaker.state_change", "name", name, "to", "OPEN").increment()
            throw e
        }
    }

    private fun onSuccess() {
        failureCount.set(0)
        successCount.incrementAndGet()
        meterRegistry.counter("circuit_breaker.success", "name", name).increment()
    }

    private fun onFailure() {
        successCount.set(0)
        val failures = failureCount.incrementAndGet()
        lastFailureTime.set(System.currentTimeMillis())
        
        meterRegistry.counter("circuit_breaker.failure", "name", name).increment()
        
        if (failures >= config.failureThreshold && state == State.CLOSED) {
            logger.warn("Circuit Breaker $name: CLOSED -> OPEN (failures: $failures)")
            state = State.OPEN
            meterRegistry.counter("circuit_breaker.state_change", "name", name, "to", "OPEN").increment()
        }
    }
    
    fun getState(): State = state
    fun getFailureCount(): Int = failureCount.get()
    fun getSuccessCount(): Int = successCount.get()
}

class CircuitBreakerOpenException(message: String) : RuntimeException(message)