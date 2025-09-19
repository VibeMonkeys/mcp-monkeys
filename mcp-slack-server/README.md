# 🚀 MCP Slack Server

A production-ready Model Context Protocol (MCP) server for Slack integration with comprehensive features including real-time messaging, thread management, and event handling.

## 📋 Features

### Core Slack Capabilities
- ✅ **Message Operations**: Send, retrieve, and search messages
- ✅ **Thread Management**: Create and reply to message threads
- ✅ **Real-time Events**: Socket Mode support for live message streaming
- ✅ **User Management**: Profile retrieval and user information
- ✅ **Channel Operations**: List channels and manage subscriptions
- ✅ **Reactions**: Add emoji reactions to messages

### Enterprise Features
- 🔐 **Security**: Input validation, rate limiting, secure token handling
- 📊 **Monitoring**: Prometheus metrics, structured logging, health checks
- 🚀 **Performance**: Async processing, connection pooling, retry logic
- 🔧 **Reliability**: Circuit breakers, graceful degradation, error handling
- 🧪 **Testing**: Comprehensive unit and integration tests

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                MCP Client (:8090)                       │
│              Spring AI Integration                      │
└─────────────────────┬───────────────────────────────────┘
                      │ HTTP/REST
                      ▼
┌─────────────────────────────────────────────────────────┐
│              Slack MCP Server (:8096)                   │
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐│
│  │   REST API      │  │   Socket Mode   │  │ Metrics  ││
│  │   Endpoints     │  │   Events        │  │ Service  ││
│  └─────────────────┘  └─────────────────┘  └──────────┘│
│  ┌─────────────────┐  ┌─────────────────┐  ┌──────────┐│
│  │   Slack API     │  │   Async Service │  │ Security ││
│  │   Service       │  │   Processing    │  │ Layer    ││
│  └─────────────────┘  └─────────────────┘  └──────────┘│
└─────────────────────┬───────────────────────────────────┘
                      │ Slack API
                      ▼
┌─────────────────────────────────────────────────────────┐
│                 Slack Workspace                         │
│        Channels, Users, Messages, Events               │
└─────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### 1. Prerequisites
- Java 21+
- Kotlin 1.9.25+
- Slack App with proper permissions

### 2. Slack App Configuration

#### OAuth & Permissions (Bot Token Scopes)
```
chat:write          - Send messages
channels:read       - List public channels
channels:history    - Read channel messages
users:read          - Get user information
reactions:write     - Add reactions
app_mentions:read   - Receive mentions
```

#### Socket Mode (for real-time events)
1. Enable Socket Mode in your Slack app
2. Generate App-Level Token with `connections:write` scope
3. Subscribe to events: `app_mention`, `message.channels`

### 3. Environment Setup
```bash
# Required
export SLACK_BOT_TOKEN="xoxb-your-bot-token"

# Optional (for real-time events)
export SLACK_APP_TOKEN="xapp-your-app-token"
export SLACK_DEFAULT_CHANNEL="general"

# Advanced
export SLACK_SOCKET_MODE_ENABLED=true
export SLACK_RATE_LIMIT_ENABLED=true
```

### 4. Run the Server
```bash
# Build and run
./gradlew :mcp-slack-server:bootRun

# Check health
curl http://localhost:8096/health
```

## 📡 API Endpoints

### MCP Tools
All endpoints use `/api/slack/` prefix:

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/sendMessage` | POST | Send a message to a channel |
| `/getMessages` | POST | Retrieve channel messages |
| `/replyToThread` | POST | Reply to a message thread |
| `/getThreadReplies` | POST | Get thread conversation |
| `/addReaction` | POST | Add emoji reaction |
| `/getUserInfo` | POST | Get user profile |
| `/listChannels` | POST | List available channels |
| `/searchMessages` | POST | Search for messages |
| `/subscribeToChannel` | POST | Subscribe to channel events |
| `/unsubscribeFromChannel` | POST | Unsubscribe from events |

### Monitoring
| Endpoint | Description |
|----------|-------------|
| `/health` | Basic health check |
| `/actuator/health` | Detailed health status |
| `/actuator/prometheus` | Prometheus metrics |
| `/mcp/status` | MCP protocol status |

## 💡 Usage Examples

### Send a Message
```bash
curl -X POST http://localhost:8096/api/slack/sendMessage \\
  -H "Content-Type: application/json" \\
  -d '{
    "channel": "general",
    "text": "Hello from MCP Slack Server! 👋"
  }'
```

### Get Recent Messages
```bash
curl -X POST http://localhost:8096/api/slack/getMessages \\
  -H "Content-Type: application/json" \\
  -d '{
    "channel": "general",
    "limit": 10,
    "includeThreads": true
  }'
```

### Reply to Thread
```bash
curl -X POST http://localhost:8096/api/slack/replyToThread \\
  -H "Content-Type: application/json" \\
  -d '{
    "channel": "general",
    "threadTs": "1234567890.123456",
    "text": "This is a thread reply",
    "broadcast": false
  }'
```

### Subscribe to Events
```bash
curl -X POST http://localhost:8096/api/slack/subscribeToChannel \\
  -H "Content-Type: application/json" \\
  -d '{
    "channel": "notifications"
  }'
```

## 🧪 Testing

### Run Tests
```bash
# Unit tests
./gradlew :mcp-slack-server:test

# Integration tests
./gradlew :mcp-slack-server:integrationTest

# Test with coverage
./gradlew :mcp-slack-server:jacocoTestReport
```

### Mock Testing
The server includes comprehensive mocking for development:
- Dummy token handling for local development
- Simulated API responses when Slack is unavailable
- Fallback mechanisms for all operations

## 📊 Monitoring & Observability

### Prometheus Metrics
- `slack_messages_sent_total` - Messages sent counter
- `slack_api_calls_total` - API calls by endpoint
- `slack_events_received_total` - Real-time events
- `slack_rate_limits_total` - Rate limit hits
- `slack_active_subscriptions` - Active channel subscriptions

### Health Checks
```bash
# Basic health
curl http://localhost:8096/health

# Detailed status
curl http://localhost:8096/actuator/health

# Configuration status
curl http://localhost:8096/mcp/status
```

### Logging
Structured JSON logging with:
- Request tracing
- Performance metrics
- Security events
- Error categorization

## 🔐 Security Features

### Input Validation
- Message length limits (4000 characters)
- Channel name validation
- User ID format checking
- Emoji name validation

### Token Security
- No token exposure in logs
- Secure configuration validation
- Rate limiting protection
- CORS policy enforcement

### Error Handling
- Sanitized error messages
- Proper HTTP status codes
- Retry mechanisms with backoff
- Circuit breaker patterns

## 🚀 Performance Optimizations

### Async Processing
```kotlin
// Coroutine-based async operations
suspend fun sendMessageAsync(request: SlackMessageRequest): SlackMessageResponse

// CompletableFuture support
@Async fun sendMessageAsync(request: SlackMessageRequest): CompletableFuture<SlackMessageResponse>
```

### Connection Management
- HTTP connection pooling
- Keep-alive connections
- Timeout configurations
- Resource cleanup

### Caching
- In-memory caching for frequently accessed data
- TTL-based cache expiration
- Cache invalidation strategies

## 🔧 Configuration

### Application Properties
```yaml
slack:
  bot-token: ${SLACK_BOT_TOKEN}
  app-token: ${SLACK_APP_TOKEN}
  default-channel: ${SLACK_DEFAULT_CHANNEL:general}
  socket-mode:
    enabled: ${SLACK_SOCKET_MODE_ENABLED:true}
  rate-limiting:
    enabled: true
    requests-per-minute: 60

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  metrics:
    export:
      prometheus:
        enabled: true
```

### Docker Support
```dockerfile
FROM openjdk:21-jre-slim
COPY mcp-slack-server.jar app.jar
EXPOSE 8096
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Integration with MCP Client

The Slack server integrates seamlessly with the MCP client ecosystem:

1. **Auto-discovery**: MCP client automatically discovers available tools
2. **Type safety**: Strongly typed request/response DTOs
3. **Error propagation**: Structured error responses
4. **Metrics integration**: Unified monitoring across all MCP servers

### MCP Client Configuration
```yaml
spring:
  ai:
    mcp:
      client:
        connections:
          slack:
            url: http://localhost:8096
            name: slack-mcp-server
```

## 📝 Development Guidelines

### Code Style
- Kotlin coding conventions
- Comprehensive documentation
- Type-safe implementations
- Functional programming patterns

### Testing Strategy
- Unit tests for business logic
- Integration tests for API endpoints
- Mocking for external dependencies
- Performance testing for critical paths

### Contributing
1. Fork the repository
2. Create feature branch
3. Add comprehensive tests
4. Update documentation
5. Submit pull request

## 🐛 Troubleshooting

### Common Issues

**Token Authentication**
```bash
# Check token configuration
curl http://localhost:8096/mcp/status

# Verify token format
echo $SLACK_BOT_TOKEN | grep -E "^xoxb-"
```

**Socket Mode Connection**
```bash
# Check app token
echo $SLACK_APP_TOKEN | grep -E "^xapp-"

# Verify event subscriptions in Slack app settings
```

**Rate Limiting**
```bash
# Monitor rate limit metrics
curl http://localhost:8096/actuator/prometheus | grep slack_rate
```

### Debug Mode
```bash
# Enable debug logging
export LOGGING_LEVEL_COM_MONKEYS_SLACK=DEBUG
./gradlew :mcp-slack-server:bootRun
```

## 📄 License

This project is part of the MCP Monkeys ecosystem and follows the same licensing terms.

## 🆘 Support

For issues and support:
1. Check the troubleshooting guide
2. Review server logs
3. Verify Slack app configuration
4. Test with minimal examples
5. Report issues with full context

---

Built with ❤️ using Spring Boot, Kotlin, and the Slack SDK.