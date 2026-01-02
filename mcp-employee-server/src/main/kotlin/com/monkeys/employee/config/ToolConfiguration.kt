package com.monkeys.employee.config

import com.monkeys.employee.adapter.`in`.mcp.EmployeeMcpAdapter
import org.springframework.ai.tool.ToolCallbackProvider
import org.springframework.ai.tool.method.MethodToolCallbackProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ToolConfiguration {

    @Bean
    fun employeeToolCallbackProvider(employeeMcpAdapter: EmployeeMcpAdapter): ToolCallbackProvider {
        return MethodToolCallbackProvider.builder()
            .toolObjects(employeeMcpAdapter)
            .build()
    }
}
