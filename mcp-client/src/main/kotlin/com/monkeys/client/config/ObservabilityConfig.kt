package com.monkeys.client.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.boot.actuate.metrics.MetricsEndpoint
import org.springframework.boot.actuate.health.HealthEndpoint
import org.springframework.boot.actuate.info.InfoEndpoint

@Configuration
class ObservabilityConfig {
    
    @Bean
    fun chatRequestCounter(meterRegistry: MeterRegistry): Counter {
        return Counter.builder("mcp.chat.requests")
            .description("Total number of chat requests")
            .tag("service", "unified-mcp-client")
            .register(meterRegistry)
    }
    
    @Bean
    fun chatRequestTimer(meterRegistry: MeterRegistry): Timer {
        return Timer.builder("mcp.chat.duration")
            .description("Chat request processing duration")
            .tag("service", "unified-mcp-client")
            .register(meterRegistry)
    }
    
    @Bean
    fun toolCallCounter(meterRegistry: MeterRegistry): Counter {
        return Counter.builder("mcp.tools.calls")
            .description("Total number of tool calls")
            .tag("service", "unified-mcp-client")
            .register(meterRegistry)
    }
    
    @Bean
    fun errorCounter(meterRegistry: MeterRegistry): Counter {
        return Counter.builder("mcp.errors")
            .description("Total number of errors")
            .tag("service", "unified-mcp-client")
            .register(meterRegistry)
    }
}