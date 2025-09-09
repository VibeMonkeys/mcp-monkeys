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
    weather: McpServerStatus;
    news: McpServerStatus;
    translate: McpServerStatus;
    calendar: McpServerStatus;
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
  apiCredentials: Record<string, string>;
  mcpServers: Record<string, McpServerStatus>;
  overallHealth: string;
  configurationInstructions: Record<string, string>;
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
  conversationId?: string;
  tools?: string[];
}

export interface ChatResponse {
  response: string;
  conversationId: string;
  tools_used?: string[];
  timestamp: number;
}

class McpApiService {
  private baseUrl = '';
  
  constructor() {
    // Vite의 프록시 설정을 사용하므로 baseUrl은 빈 문자열
  }

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
      throw error;
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
      throw error;
    }
  }

  async getConfigStatus() {
    try {
      const response = await fetch('/api/health/config');
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }
      return await response.json();
    } catch (error) {
      console.error('설정 상태 확인 오류:', error);
      throw error;
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
        body: JSON.stringify(request)
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error('채팅 메시지 전송 오류:', error);
      throw error;
    }
  }

  // MCP Tool APIs
  async getWeather(city: string) {
    return this.callTool('getWeather', { city });
  }

  async searchNews(query: string, category?: string) {
    return this.callTool('searchNews', { query, category });
  }

  async getNewsHeadlines(country?: string, category?: string) {
    return this.callTool('getNewsHeadlines', { country, category });
  }

  async translateText(text: string, targetLanguage: string, sourceLanguage?: string) {
    return this.callTool('translateText', { text, targetLanguage, sourceLanguage });
  }

  async detectLanguage(text: string) {
    return this.callTool('detectLanguage', { text });
  }

  async createCalendarEvent(title: string, date: string, startTime?: string, endTime?: string, description?: string) {
    return this.callTool('createCalendarEvent', { title, date, startTime, endTime, description });
  }

  async getCalendarEvents(date: string) {
    return this.callTool('getCalendarEvents', { date });
  }

  async updateCalendarEvent(eventId: string, updates: any) {
    return this.callTool('updateCalendarEvent', { eventId, ...updates });
  }

  async deleteCalendarEvent(eventId: string) {
    return this.callTool('deleteCalendarEvent', { eventId });
  }

  // Generic tool call method
  private async callTool(toolName: string, parameters: Record<string, any>) {
    try {
      const response = await fetch('/api/tools/call', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          tool: toolName,
          parameters
        })
      });

      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`);
      }

      return await response.json();
    } catch (error) {
      console.error(`도구 호출 오류 (${toolName}):`, error);
      throw error;
    }
  }

  // Connection test
  async testConnection(): Promise<boolean> {
    try {
      const response = await fetch('/api/health', { method: 'GET' });
      return response.ok;
    } catch {
      return false;
    }
  }

  // Get available tools
  async getAvailableTools() {
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
}

export const mcpApi = new McpApiService();
export default mcpApi;