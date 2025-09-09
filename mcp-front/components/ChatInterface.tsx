import { useState, useEffect, useRef } from "react";
import { Sidebar } from "./Sidebar";
import { MessageBubble, Message } from "./MessageBubble";
import { MessageInput } from "./MessageInput";
import { ScrollArea } from "./ui/scroll-area";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Separator } from "./ui/separator";
import { toast } from "sonner";
import { motion } from "motion/react";
import { Server, Zap, Clock, AlertCircle, CheckCircle, RefreshCw, Activity } from "lucide-react";
import { mcpApi, McpHealthResponse, McpComprehensiveHealth } from "../services/mcpApi";

export function ChatInterface() {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      content: '안녕하세요! MCP Client에 오신 것을 환영합니다. 이 인터페이스를 통해 MCP 서버와 안전하게 통신할 수 있습니다. 어떤 작업을 도와드릴까요?',
      sender: 'assistant',
      timestamp: new Date(),
      type: 'text'
    }
  ]);
  
  const [isLoading, setIsLoading] = useState(false);
  const [connectionStatus, setConnectionStatus] = useState<'connected' | 'disconnected' | 'checking'>('checking');
  const [serverHealth, setServerHealth] = useState<McpHealthResponse | null>(null);
  const [comprehensiveHealth, setComprehensiveHealth] = useState<McpComprehensiveHealth | null>(null);
  const [conversationId, setConversationId] = useState<string>('');
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const abortControllerRef = useRef<AbortController | null>(null);

  // 연결 상태 확인
  const checkConnection = async () => {
    setConnectionStatus('checking');
    try {
      const isConnected = await mcpApi.testConnection();
      setConnectionStatus(isConnected ? 'connected' : 'disconnected');
      
      if (isConnected) {
        // 서버 헬스 정보 가져오기
        try {
          const [health, comprehensive] = await Promise.all([
            mcpApi.getServerHealth(),
            mcpApi.getComprehensiveHealth()
          ]);
          setServerHealth(health);
          setComprehensiveHealth(comprehensive);
        } catch (error) {
          console.warn('헬스체크 정보 가져오기 실패:', error);
        }
      }
    } catch (error) {
      console.error('연결 확인 중 오류:', error);
      setConnectionStatus('disconnected');
    }
  };

  // 초기 연결 확인 및 주기적 체크
  useEffect(() => {
    checkConnection();
    
    const interval = setInterval(checkConnection, 30000); // 30초마다 체크
    return () => clearInterval(interval);
  }, []);

  // 메시지 스크롤
  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  // 메시지 전송
  const handleSendMessage = async (content: string) => {
    if (!content.trim() || isLoading) return;
    
    if (connectionStatus !== 'connected') {
      toast.error('MCP 서버에 연결되지 않았습니다. 연결을 확인해주세요.');
      return;
    }

    const userMessage: Message = {
      id: Date.now().toString(),
      content: content.trim(),
      sender: 'user',
      timestamp: new Date(),
      type: 'text'
    };

    setMessages(prev => [...prev, userMessage]);
    setIsLoading(true);

    // 이전 요청이 있으면 취소
    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    
    abortControllerRef.current = new AbortController();

    try {
      const chatResponse = await mcpApi.sendChatMessage({
        message: content.trim(),
        conversationId: conversationId || undefined
      });

      // conversation ID 업데이트
      if (chatResponse.conversationId && !conversationId) {
        setConversationId(chatResponse.conversationId);
      }

      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: chatResponse.response,
        sender: 'assistant',
        timestamp: new Date(),
        type: 'text',
        toolsUsed: chatResponse.tools_used
      };

      setMessages(prev => [...prev, assistantMessage]);
      
      // 도구 사용 시 토스트 알림
      if (chatResponse.tools_used && chatResponse.tools_used.length > 0) {
        toast.success(`사용된 도구: ${chatResponse.tools_used.join(', ')}`);
      }

    } catch (error: any) {
      console.error('메시지 전송 오류:', error);
      
      if (error.name !== 'AbortError') {
        const errorMessage: Message = {
          id: (Date.now() + 1).toString(),
          content: `죄송합니다. 요청 처리 중 오류가 발생했습니다: ${error.message}`,
          sender: 'assistant',
          timestamp: new Date(),
          type: 'error'
        };
        setMessages(prev => [...prev, errorMessage]);
        toast.error('메시지 전송 실패');
      }
    } finally {
      setIsLoading(false);
      abortControllerRef.current = null;
    }
  };

  // 대화 초기화
  const handleClearChat = () => {
    setMessages([{
      id: '1',
      content: '안녕하세요! MCP Client에 오신 것을 환영합니다. 이 인터페이스를 통해 MCP 서버와 안전하게 통신할 수 있습니다. 어떤 작업을 도와드릴까요?',
      sender: 'assistant',
      timestamp: new Date(),
      type: 'text'
    }]);
    setConversationId('');
    toast.success('대화가 초기화되었습니다');
  };

  // 연결 상태 배지
  const getConnectionBadge = () => {
    const statusConfig = {
      connected: { 
        variant: 'default' as const, 
        icon: CheckCircle, 
        text: '연결됨',
        color: 'text-green-600'
      },
      disconnected: { 
        variant: 'destructive' as const, 
        icon: AlertCircle, 
        text: '연결 끊김',
        color: 'text-red-600'
      },
      checking: { 
        variant: 'secondary' as const, 
        icon: RefreshCw, 
        text: '확인 중...',
        color: 'text-yellow-600'
      }
    };

    const config = statusConfig[connectionStatus];
    const Icon = config.icon;

    return (
      <Badge variant={config.variant} className="gap-1">
        <Icon className={`h-3 w-3 ${config.color} ${connectionStatus === 'checking' ? 'animate-spin' : ''}`} />
        {config.text}
      </Badge>
    );
  };

  // 서버 상태 카드
  const renderServerStatus = () => {
    if (!serverHealth) return null;

    return (
      <Card className="mb-4">
        <CardHeader className="pb-2">
          <CardTitle className="text-sm flex items-center gap-2">
            <Server className="h-4 w-4" />
            MCP 서버 상태
            <Button 
              variant="ghost" 
              size="sm" 
              onClick={checkConnection}
              disabled={connectionStatus === 'checking'}
            >
              <RefreshCw className={`h-3 w-3 ${connectionStatus === 'checking' ? 'animate-spin' : ''}`} />
            </Button>
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="grid grid-cols-2 gap-2 text-xs">
            {Object.entries(serverHealth.servers).map(([name, status]) => (
              <div key={name} className="flex items-center justify-between p-2 rounded bg-muted/50">
                <span className="font-medium capitalize">{name}</span>
                <div className="flex items-center gap-1">
                  {status.status === 'UP' ? (
                    <CheckCircle className="h-3 w-3 text-green-600" />
                  ) : (
                    <AlertCircle className="h-3 w-3 text-red-600" />
                  )}
                  <span className="text-muted-foreground">{status.responseTime}</span>
                </div>
              </div>
            ))}
          </div>
          <Separator className="my-2" />
          <div className="text-center">
            <Badge variant={serverHealth.overallStatus === 'UP' ? 'default' : 'destructive'}>
              전체 상태: {serverHealth.overallStatus}
            </Badge>
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <div className="flex h-screen bg-background">
      {/* 사이드바 */}
      <Sidebar 
        isOpen={isSidebarOpen} 
        onToggle={() => setIsSidebarOpen(!isSidebarOpen)}
        onClearChat={handleClearChat}
        serverHealth={comprehensiveHealth}
      />
      
      {/* 메인 채팅 영역 */}
      <div className="flex-1 flex flex-col">
        {/* 헤더 */}
        <div className="border-b bg-card p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center gap-3">
              <Button
                variant="ghost"
                size="sm"
                onClick={() => setIsSidebarOpen(!isSidebarOpen)}
                className="md:hidden"
              >
                <Server className="h-4 w-4" />
              </Button>
              <div>
                <h1 className="font-semibold">MCP Client Interface</h1>
                <p className="text-sm text-muted-foreground">
                  통합 MCP 서버와의 대화형 인터페이스
                </p>
              </div>
            </div>
            <div className="flex items-center gap-2">
              {getConnectionBadge()}
              {conversationId && (
                <Badge variant="outline" className="text-xs">
                  대화 ID: {conversationId.slice(-8)}
                </Badge>
              )}
            </div>
          </div>
        </div>

        {/* 메시지 영역 */}
        <div className="flex-1 flex">
          {/* 왼쪽: 채팅 메시지들 */}
          <div className="flex-1 flex flex-col">
            <ScrollArea className="flex-1 p-4">
              <div className="space-y-4 max-w-4xl mx-auto">
                {messages.map((message) => (
                  <MessageBubble key={message.id} message={message} />
                ))}
                {isLoading && (
                  <motion.div
                    initial={{ opacity: 0, y: 20 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="flex justify-start"
                  >
                    <div className="bg-muted rounded-lg px-4 py-2 max-w-xs">
                      <div className="flex items-center gap-2 text-muted-foreground">
                        <div className="flex gap-1">
                          <div className="w-2 h-2 bg-current rounded-full animate-bounce [animation-delay:-0.3s]" />
                          <div className="w-2 h-2 bg-current rounded-full animate-bounce [animation-delay:-0.15s]" />
                          <div className="w-2 h-2 bg-current rounded-full animate-bounce" />
                        </div>
                        <span className="text-sm">처리 중...</span>
                      </div>
                    </div>
                  </motion.div>
                )}
                <div ref={messagesEndRef} />
              </div>
            </ScrollArea>

            {/* 메시지 입력 */}
            <div className="border-t bg-card p-4">
              <div className="max-w-4xl mx-auto">
                <MessageInput
                  onSendMessage={handleSendMessage}
                  isLoading={isLoading}
                  disabled={connectionStatus !== 'connected'}
                  placeholder={
                    connectionStatus === 'connected' 
                      ? "메시지를 입력하세요..." 
                      : "서버에 연결 중입니다..."
                  }
                />
              </div>
            </div>
          </div>

          {/* 오른쪽: 상태 패널 (데스크톱에서만) */}
          <div className="hidden lg:block w-80 border-l bg-card/50 p-4">
            <div className="space-y-4">
              {renderServerStatus()}
              
              {/* 빠른 액션 */}
              <Card>
                <CardHeader className="pb-2">
                  <CardTitle className="text-sm flex items-center gap-2">
                    <Zap className="h-4 w-4" />
                    빠른 액션
                  </CardTitle>
                </CardHeader>
                <CardContent className="pt-0 space-y-2">
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full justify-start"
                    onClick={() => handleSendMessage("서울 날씨 알려줘")}
                    disabled={connectionStatus !== 'connected'}
                  >
                    <Activity className="h-3 w-3 mr-2" />
                    날씨 확인
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full justify-start"
                    onClick={() => handleSendMessage("최신 뉴스 헤드라인 보여줘")}
                    disabled={connectionStatus !== 'connected'}
                  >
                    <Activity className="h-3 w-3 mr-2" />
                    뉴스 확인
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full justify-start"
                    onClick={() => handleSendMessage("Hello를 한국어로 번역해줘")}
                    disabled={connectionStatus !== 'connected'}
                  >
                    <Activity className="h-3 w-3 mr-2" />
                    번역 테스트
                  </Button>
                  <Button 
                    variant="outline" 
                    size="sm" 
                    className="w-full justify-start"
                    onClick={() => handleSendMessage("오늘 일정 확인해줘")}
                    disabled={connectionStatus !== 'connected'}
                  >
                    <Activity className="h-3 w-3 mr-2" />
                    일정 확인
                  </Button>
                </CardContent>
              </Card>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}