package com.monkeys.client.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import jakarta.validation.constraints.NotBlank

@Configuration
@EnableConfigurationProperties(ApiCredentials::class, McpServerUrls::class)
class SecurityConfig

@Component
@ConfigurationProperties(prefix = "api")
@Validated
data class ApiCredentials(
    val github: GitHubCredentials = GitHubCredentials(),
    val jira: JiraCredentials = JiraCredentials(),
    val gmail: GmailCredentials = GmailCredentials(),
    val slack: SlackCredentials = SlackCredentials()
) {
    data class GitHubCredentials(
        val token: String = "dummy-token"
    ) {
        fun isConfigured(): Boolean = token != "dummy-token" && token.isNotBlank()
    }
    
    data class JiraCredentials(
        val url: String = "https://your-domain.atlassian.net",
        val email: String = "dummy@email.com", 
        val token: String = "dummy-token"
    ) {
        fun isConfigured(): Boolean = 
            !url.contains("your-domain") && 
            !email.contains("dummy") && 
            token != "dummy-token" && 
            token.isNotBlank()
    }
    
    data class GmailCredentials(
        val clientId: String = "dummy-client-id",
        val clientSecret: String = "dummy-client-secret",
        val refreshToken: String = "dummy-refresh-token"
    ) {
        fun isConfigured(): Boolean = 
            !clientId.contains("dummy") && 
            !clientSecret.contains("dummy") && 
            !refreshToken.contains("dummy")
    }
    
    data class SlackCredentials(
        val botToken: String = "xoxb-dummy-token"
    ) {
        fun isConfigured(): Boolean = !botToken.contains("dummy") && botToken.startsWith("xoxb-")
    }
}

@Component
@ConfigurationProperties(prefix = "mcp")
@Validated  
data class McpServerUrls(
    val weather: ServerConfig = ServerConfig("http://localhost:8092"),
    val news: ServerConfig = ServerConfig("http://localhost:8093"),
    val translate: ServerConfig = ServerConfig("http://localhost:8094"),
    val calendar: ServerConfig = ServerConfig("http://localhost:8095")
) {
    data class ServerConfig(
        @field:NotBlank
        val url: String
    )
}

@Component
class ConfigurationValidator(
    private val apiCredentials: ApiCredentials
) {
    
    fun validateApiConfiguration(): Map<String, String> {
        return mapOf(
            "GitHub" to if (apiCredentials.github.isConfigured()) "✅ Configured" else "❌ Not Configured",
            "Jira" to if (apiCredentials.jira.isConfigured()) "✅ Configured" else "❌ Not Configured", 
            "Gmail" to if (apiCredentials.gmail.isConfigured()) "✅ Configured" else "❌ Not Configured",
            "Slack" to if (apiCredentials.slack.isConfigured()) "✅ Configured" else "❌ Not Configured"
        )
    }
    
    fun getConfigurationInstructions(): Map<String, String> {
        val instructions = mutableMapOf<String, String>()
        
        if (!apiCredentials.github.isConfigured()) {
            instructions["GitHub"] = "Set GITHUB_TOKEN environment variable with your GitHub personal access token"
        }
        
        if (!apiCredentials.jira.isConfigured()) {
            instructions["Jira"] = "Set JIRA_URL, JIRA_EMAIL, and JIRA_TOKEN environment variables"
        }
        
        if (!apiCredentials.gmail.isConfigured()) {
            instructions["Gmail"] = "Set GMAIL_CLIENT_ID, GMAIL_CLIENT_SECRET, and GMAIL_REFRESH_TOKEN environment variables"
        }
        
        if (!apiCredentials.slack.isConfigured()) {
            instructions["Slack"] = "Set SLACK_BOT_TOKEN environment variable with your Slack bot token"
        }
        
        return instructions
    }
}