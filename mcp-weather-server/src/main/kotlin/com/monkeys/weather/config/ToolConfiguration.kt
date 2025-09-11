package com.monkeys.weather.config

import com.monkeys.weather.service.WeatherMcpService
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Weather MCP Tool 설정 클래스
 * MCP 도구 등록 및 설정을 담당
 */
@Configuration
class ToolConfiguration {

    /**
     * Weather MCP Tools 등록
     * WeatherMcpService의 @Tool 어노테이션이 있는 메서드들을 자동으로 스캔하여 등록
     */
    @Bean
    fun weatherTools(weatherMcpService: WeatherMcpService): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(weatherMcpService)
            .build()
    }
}