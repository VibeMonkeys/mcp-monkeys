import { useState } from "react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Separator } from "./ui/separator";
import { 
  MessageSquare, 
  Settings, 
  Activity, 
  Database, 
  Shield, 
  Plus,
  ChevronLeft,
  ChevronRight,
  Circle
} from "lucide-react";
import { cn } from "./ui/utils";

interface SidebarProps {
  isConnected: boolean;
  onNewChat: () => void;
  onSettings: () => void;
}

export function Sidebar({ isConnected, onNewChat, onSettings }: SidebarProps) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  
  const conversations = [
    { id: '1', title: 'MCP 서버 연결 테스트', lastMessage: '방금 전', isActive: true },
    { id: '2', title: '데이터베이스 쿼리', lastMessage: '5분 전', isActive: false },
    { id: '3', title: 'API 호출 테스트', lastMessage: '1시간 전', isActive: false },
  ];

  return (
    <div className={cn(
      "relative h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 border-r border-slate-700/50 transition-all duration-300",
      isCollapsed ? "w-16" : "w-80"
    )}>
      {/* Background Effects */}
      <div className="absolute inset-0 bg-gradient-to-br from-blue-500/10 via-transparent to-purple-500/10" />
      <div className="absolute inset-0 backdrop-blur-3xl bg-black/20" />
      
      {/* Content */}
      <div className="relative z-10 flex flex-col h-full p-4">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          {!isCollapsed && (
            <div className="flex items-center gap-3">
              <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-blue-500 to-purple-600 flex items-center justify-center">
                <Database className="h-4 w-4 text-white" />
              </div>
              <div>
                <h1 className="text-white font-semibold">MCP Client</h1>
                <div className="flex items-center gap-2">
                  <Circle className={cn("h-2 w-2", isConnected ? "fill-green-400 text-green-400" : "fill-red-400 text-red-400")} />
                  <span className="text-xs text-slate-400">
                    {isConnected ? "연결됨" : "연결 끊김"}
                  </span>
                </div>
              </div>
            </div>
          )}
          
          <Button
            variant="ghost"
            size="sm"
            onClick={() => setIsCollapsed(!isCollapsed)}
            className="text-slate-400 hover:text-white hover:bg-slate-700/50"
          >
            {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
          </Button>
        </div>

        {/* New Chat Button */}
        <Button
          onClick={onNewChat}
          className="w-full mb-6 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 text-white border-0"
        >
          <Plus className="h-4 w-4 mr-2" />
          {!isCollapsed && "새로운 채팅"}
        </Button>

        {/* Navigation */}
        {!isCollapsed && (
          <>
            <div className="space-y-2 mb-6">
              <Button variant="ghost" className="w-full justify-start text-slate-300 hover:text-white hover:bg-slate-700/50">
                <MessageSquare className="h-4 w-4 mr-3" />
                채팅
              </Button>
              <Button variant="ghost" className="w-full justify-start text-slate-400 hover:text-white hover:bg-slate-700/50">
                <Activity className="h-4 w-4 mr-3" />
                모니터링
              </Button>
              <Button variant="ghost" className="w-full justify-start text-slate-400 hover:text-white hover:bg-slate-700/50">
                <Shield className="h-4 w-4 mr-3" />
                보안
              </Button>
            </div>

            <Separator className="bg-slate-700/50 mb-4" />

            {/* Recent Conversations */}
            <div className="flex-1 overflow-hidden">
              <h3 className="text-xs text-slate-400 uppercase tracking-wider mb-3">최근 대화</h3>
              <div className="space-y-1 overflow-y-auto">
                {conversations.map((conv) => (
                  <div
                    key={conv.id}
                    className={cn(
                      "p-3 rounded-lg cursor-pointer transition-colors",
                      conv.isActive 
                        ? "bg-slate-700/50 border border-slate-600/50" 
                        : "hover:bg-slate-700/30"
                    )}
                  >
                    <div className="text-sm text-white truncate mb-1">{conv.title}</div>
                    <div className="text-xs text-slate-400">{conv.lastMessage}</div>
                  </div>
                ))}
              </div>
            </div>
          </>
        )}

        {/* Settings */}
        <div className="mt-auto pt-4">
          <Button
            variant="ghost"
            onClick={onSettings}
            className="w-full justify-start text-slate-400 hover:text-white hover:bg-slate-700/50"
          >
            <Settings className="h-4 w-4 mr-3" />
            {!isCollapsed && "설정"}
          </Button>
        </div>
      </div>
    </div>
  );
}