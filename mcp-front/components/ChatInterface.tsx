import { useState, useEffect, useRef } from "react";
import { Sidebar } from "./Sidebar";
import { MessageBubble, Message } from "./MessageBubble";
import { MessageInput } from "./MessageInput";
import { ScrollArea } from "./ui/scroll-area";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { toast } from "sonner";
import { motion } from "motion/react";
import { Server, Zap, Clock } from "lucide-react";

// Mock API functions - 실제 Spring Boot 백엔드와 연동 시 대체해야 함
const mockApi = {
  sendMessage: async (content: string): Promise<string> => {
    await new Promise(resolve => setTimeout(resolve, 1000 + Math.random() * 2000));
    
    const responses = [
      "MCP 서버에서 요청을 성공적으로 처리했습니다. 연결 상태가 안정적이며 모든 시스템이 정상 작동 중입니다.",
      "데이터베이스 쿼리가 완료되었습니다. 총 1,247개의 레코드를 확인했으며, 최신 데이터로 업데이트되어 있습니다.",
      "API 엔드포인트 테스트 결과: 응답 시간 45ms, 상태 코드 200. 모든 기능이 정상적으로 작동합니다.",
      "보안 검사를 완료했습니다. 인증 토큰이 유효하며, 접근 권한이 확인되었습니다.",
      `"${content}"에 대한 분석을 완료했습니다. 추가적인 정보나 도움이 필요하시면 언제든 말씀해주세요.`
    ];
    
    return responses[Math.floor(Math.random() * responses.length)];
  },
  
  checkConnection: async (): Promise<boolean> => {
    await new Promise(resolve => setTimeout(resolve, 500));
    return Math.random() > 0.1;
  }
};

export function ChatInterface() {
  const [messages, setMessages] = useState<Message[]>([
    {
      id: '1',
      content: '안녕하세요! MCP Client에 오신 것을 환영합니다. 이 인터페이스를 통해 MCP 서버와 안전하게 통신할 수 있습니다. 어떤 작업을 도와드릴까요?',
      timestamp: new Date(),
      sender: 'assistant'
    }
  ]);
  const [isConnected, setIsConnected] = useState(true);
  const [isProcessing, setIsProcessing] = useState(false);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  const handleSendMessage = async (content: string) => {
    const userMessage: Message = {
      id: Date.now().toString(),
      content,
      timestamp: new Date(),
      sender: 'user',
      status: 'sending'
    };

    setMessages(prev => [...prev, userMessage]);
    setIsProcessing(true);

    try {
      setMessages(prev => 
        prev.map(msg => 
          msg.id === userMessage.id 
            ? { ...msg, status: 'sent' as const }
            : msg
        )
      );

      const response = await mockApi.sendMessage(content);
      
      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        content: response,
        timestamp: new Date(),
        sender: 'assistant'
      };

      setMessages(prev => [...prev, assistantMessage]);
    } catch (error) {
      console.error('메시지 전송 실패:', error);
      
      setMessages(prev => 
        prev.map(msg => 
          msg.id === userMessage.id 
            ? { ...msg, status: 'error' as const }
            : msg
        )
      );

      toast.error('메시지 전송에 실패했습니다.');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleNewChat = () => {
    setMessages([{
      id: Date.now().toString(),
      content: '새로운 대화를 시작합니다. 무엇을 도와드릴까요?',
      timestamp: new Date(),
      sender: 'assistant'
    }]);
    toast.success('새로운 채팅을 시작했습니다.');
  };

  const handleSettings = () => {
    toast.info('설정 페이지가 곧 추가될 예정입니다.');
  };

  const handleStopProcessing = () => {
    setIsProcessing(false);
    toast.info('처리를 중단했습니다.');
  };

  return (
    <div className="flex h-screen bg-gradient-to-br from-slate-50 via-blue-50/30 to-purple-50/30">
      {/* Sidebar */}
      <Sidebar 
        isConnected={isConnected}
        onNewChat={handleNewChat}
        onSettings={handleSettings}
      />
      
      {/* Main Chat Area */}
      <div className="flex-1 flex flex-col">
        {/* Header */}
        <div className="bg-white/80 backdrop-blur-sm border-b border-slate-200/50 px-6 py-4">
          <div className="flex items-center justify-between max-w-4xl mx-auto">
            <div className="flex items-center gap-4">
              <div>
                <h2 className="text-lg font-semibold text-slate-800">MCP Server Communication</h2>
                <p className="text-sm text-slate-500">실시간 서버 통신 인터페이스</p>
              </div>
            </div>
            
            <div className="flex items-center gap-3">
              <Badge 
                variant={isConnected ? "default" : "destructive"}
                className={isConnected ? "bg-green-100 text-green-800 border-green-200" : ""}
              >
                <Server className="h-3 w-3 mr-1" />
                {isConnected ? "연결됨" : "연결 끊김"}
              </Badge>
              
              <div className="flex items-center gap-1 text-xs text-slate-500">
                <Clock className="h-3 w-3" />
                <span>응답시간: 45ms</span>
              </div>
            </div>
          </div>
        </div>
        
        {/* Messages Area */}
        <ScrollArea className="flex-1 bg-gradient-to-b from-transparent to-slate-50/30">
          <div className="px-6 py-8">
            <div className="max-w-4xl mx-auto">
              {messages.map((message) => (
                <MessageBubble key={message.id} message={message} />
              ))}
              
              {isProcessing && (
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  className="flex justify-start max-w-4xl mx-auto mb-6"
                >
                  <div className="flex items-center gap-4">
                    <div className="h-10 w-10 rounded-full bg-gradient-to-br from-slate-600 to-slate-700 flex items-center justify-center">
                      <Zap className="h-5 w-5 text-white animate-pulse" />
                    </div>
                    <div className="bg-white border border-slate-200/50 rounded-2xl px-6 py-4 shadow-lg">
                      <div className="flex items-center gap-2 text-slate-600">
                        <div className="flex gap-1">
                          <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" />
                          <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.1s' }} />
                          <div className="w-2 h-2 bg-blue-500 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }} />
                        </div>
                        <span className="text-sm">처리중입니다...</span>
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}
              
              <div ref={messagesEndRef} />
            </div>
          </div>
        </ScrollArea>
        
        {/* Input Area */}
        <MessageInput 
          onSendMessage={handleSendMessage}
          disabled={!isConnected}
          isProcessing={isProcessing}
          onStopProcessing={handleStopProcessing}
        />
      </div>
    </div>
  );
}