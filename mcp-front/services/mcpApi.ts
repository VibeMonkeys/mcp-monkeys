// MCP Client API Integration Layer
export interface McpServerStatus {
  status: string;
  url: string;
  responseTime: string;
  circuitBreakerState: string;
  error?: string;
}

export interface McpHealthResponse {
  servers: {
    library: McpServerStatus;
    todo: McpServerStatus;
    employee: McpServerStatus;
    product: McpServerStatus;
  };
  overallStatus: string;
  timestamp: number;
}

export interface McpComprehensiveHealth {
  service: {
    name: string;
    version: string;
    status: string;
  };
  mcpServers: Record<string, McpServerStatus>;
  overallHealth: string;
  timestamp: number;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  timestamp: number;
  tools?: string[];
}

export interface ChatRequest {
  message: string;
  sessionId?: string;
  tools?: string[];
}

export interface ChatResponse {
  response: string;
  sessionId?: string;
  tools_used?: string[];
  timestamp: number;
}

export interface ToolInfo {
  name: string;
  description: string;
}

export interface ToolsResponse {
  data: Record<string, ToolInfo[]>;
  success: boolean;
  message: string;
}

class McpApiService {
  // Health Check APIs
  async getServerHealth(): Promise<McpHealthResponse> {
    try {
      const response = await fetch('/api/health/mcp-servers');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('MCP 서버 헬스체크 오류:', error);
      // Fallback dummy data
      return {
        servers: {
          library: { status: 'UNKNOWN', url: 'localhost:8091', responseTime: '-', circuitBreakerState: 'UNKNOWN' },
          todo: { status: 'UNKNOWN', url: 'localhost:8096', responseTime: '-', circuitBreakerState: 'UNKNOWN' },
          employee: { status: 'UNKNOWN', url: 'localhost:8097', responseTime: '-', circuitBreakerState: 'UNKNOWN' },
          product: { status: 'UNKNOWN', url: 'localhost:8098', responseTime: '-', circuitBreakerState: 'UNKNOWN' }
        },
        overallStatus: 'UNKNOWN',
        timestamp: Date.now()
      };
    }
  }

  async getComprehensiveHealth(): Promise<McpComprehensiveHealth> {
    try {
      const response = await fetch('/api/health/comprehensive');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('종합 헬스체크 오류:', error);
      return {
        service: {
          name: 'unified-mcp-client',
          version: '2.0.0',
          status: 'UNKNOWN'
        },
        mcpServers: {},
        overallHealth: 'UNKNOWN',
        timestamp: Date.now()
      };
    }
  }

  // Chat API
  async sendChatMessage(request: ChatRequest): Promise<ChatResponse> {
    try {
      const response = await fetch('/api/chat', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: request.message,
          sessionId: request.sessionId
        })
      });

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(`HTTP ${response.status}: ${response.statusText} - ${errorText}`);
      }

      const result = await response.json();

      if (result.success && result.data) {
        return {
          response: result.data.response,
          sessionId: result.data.sessionId,
          tools_used: result.data.tools_used,
          timestamp: result.timestamp || Date.now()
        };
      } else {
        throw new Error(result.message || '알 수 없는 오류가 발생했습니다.');
      }
    } catch (error) {
      console.error('채팅 메시지 전송 오류:', error);
      throw error;
    }
  }

  // Connection test
  async testConnection(): Promise<boolean> {
    try {
      const response = await fetch('/api/status');
      return response.ok;
    } catch {
      return false;
    }
  }

  // Get available tools (dynamic from backend)
  async getAvailableTools(): Promise<ToolsResponse> {
    try {
      const response = await fetch('/api/tools');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('사용 가능한 도구 조회 오류:', error);
      throw error;
    }
  }

  // Get tools summary
  async getToolsSummary(): Promise<Record<string, number>> {
    try {
      const response = await fetch('/api/tools/summary');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      const result = await response.json();
      return result.data || {};
    } catch (error) {
      console.error('도구 요약 조회 오류:', error);
      return {};
    }
  }

  // Get service status
  async getServiceStatus() {
    try {
      const response = await fetch('/api/status');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('서비스 상태 조회 오류:', error);
      throw error;
    }
  }
}

export const mcpApi = new McpApiService();
export default mcpApi;
