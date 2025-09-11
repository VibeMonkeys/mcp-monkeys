import { useState } from "react";
import { Button } from "./ui/button";
import { Badge } from "./ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Separator } from "./ui/separator";
import { 
  Settings, 
  Activity, 
  Database, 
  Plus,
  ChevronLeft,
  ChevronRight,
  Server,
  CheckCircle,
  AlertCircle,
  X,
  Trash2,
  BarChart3
} from "lucide-react";
import { cn } from "./ui/utils";
import { McpComprehensiveHealth } from "../services/mcpApi";

interface SidebarProps {
  isOpen: boolean;
  onToggle: () => void;
  onClearChat: () => void;
  serverHealth?: McpComprehensiveHealth | null;
}

export function Sidebar({ isOpen, onToggle, onClearChat, serverHealth }: SidebarProps) {
  const [isCollapsed, setIsCollapsed] = useState(false);
  
  if (!isOpen) {
    return null;
  }

  return (
    <>
      {/* Mobile Overlay */}
      <div 
        className="fixed inset-0 bg-black/50 z-40 md:hidden"
        onClick={onToggle}
      />
      
      {/* Sidebar */}
      <div className={cn(
        "fixed left-0 top-0 h-screen bg-gradient-to-b from-slate-900 via-slate-800 to-slate-900 border-r border-slate-700/50 transition-all duration-300 z-50 md:relative",
        isCollapsed ? "w-16" : "w-80"
      )}>
        {/* Background Effects */}
        <div className="absolute inset-0 bg-gradient-to-br from-blue-500/10 via-transparent to-purple-500/10" />
        <div className="absolute inset-0 backdrop-blur-3xl bg-black/20" />
        
        {/* Content */}
        <div className="relative z-10 flex flex-col h-full text-white">
          {/* Header */}
          <div className="p-4">
            <div className="flex items-center justify-between">
              {!isCollapsed && (
                <div className="flex items-center gap-2">
                  <Database className="h-6 w-6 text-blue-400" />
                  <h2 className="font-semibold text-lg">MCP Client</h2>
                </div>
              )}
              <div className="flex items-center gap-1">
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={() => setIsCollapsed(!isCollapsed)}
                  className="text-slate-400 hover:text-white hover:bg-white/10"
                >
                  {isCollapsed ? <ChevronRight className="h-4 w-4" /> : <ChevronLeft className="h-4 w-4" />}
                </Button>
                <Button
                  variant="ghost"
                  size="sm"
                  onClick={onToggle}
                  className="text-slate-400 hover:text-white hover:bg-white/10 md:hidden"
                >
                  <X className="h-4 w-4" />
                </Button>
              </div>
            </div>
          </div>

          {!isCollapsed && (
            <>
              {/* Actions */}
              <div className="px-4 mb-4 space-y-2">
                <Button 
                  onClick={onClearChat}
                  className="w-full justify-start bg-blue-600 hover:bg-blue-700 text-white"
                >
                  <Plus className="h-4 w-4 mr-2" />
                  새로운 대화
                </Button>
                <Button 
                  variant="outline" 
                  className="w-full justify-start border-slate-600 text-slate-300 hover:bg-white/10"
                  onClick={onClearChat}
                >
                  <Trash2 className="h-4 w-4 mr-2" />
                  대화 초기화
                </Button>
              </div>

              <Separator className="bg-slate-700/50" />

              {/* Server Health Status */}
              {serverHealth && (
                <div className="p-4">
                  <Card className="bg-slate-800/50 border-slate-600">
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm text-slate-200 flex items-center gap-2">
                        <Server className="h-4 w-4" />
                        서버 상태
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-0">
                      {/* Service Status */}
                      <div className="mb-3">
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-slate-400">메인 서비스</span>
                          <Badge 
                            variant={serverHealth.service.status === 'UP' ? 'default' : 'destructive'}
                            className="text-xs"
                          >
                            {serverHealth.service.status}
                          </Badge>
                        </div>
                      </div>

                      {/* Overall Health */}
                      <div className="mb-3">
                        <div className="flex items-center justify-between text-xs">
                          <span className="text-slate-400">전체 상태</span>
                          <Badge 
                            variant={serverHealth.overallHealth === 'HEALTHY' ? 'default' : 'destructive'}
                            className="text-xs"
                          >
                            {serverHealth.overallHealth}
                          </Badge>
                        </div>
                      </div>

                      {/* MCP Servers */}
                      <div className="space-y-2">
                        <span className="text-xs text-slate-400">MCP 서버</span>
                        {Object.entries(serverHealth.mcpServers).map(([name, status]) => (
                          <div key={name} className="flex items-center justify-between text-xs">
                            <div className="flex items-center gap-2">
                              {status.status === 'UP' ? (
                                <CheckCircle className="h-3 w-3 text-green-500" />
                              ) : (
                                <AlertCircle className="h-3 w-3 text-red-500" />
                              )}
                              <span className="text-slate-300 capitalize">{name}</span>
                            </div>
                            <span className="text-slate-500 text-xs">{status.responseTime}</span>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                </div>
              )}

              <Separator className="bg-slate-700/50" />

              {/* Configuration Status */}
              {serverHealth?.apiCredentials && (
                <div className="p-4">
                  <Card className="bg-slate-800/50 border-slate-600">
                    <CardHeader className="pb-2">
                      <CardTitle className="text-sm text-slate-200 flex items-center gap-2">
                        <Settings className="h-4 w-4" />
                        API 설정
                      </CardTitle>
                    </CardHeader>
                    <CardContent className="pt-0">
                      <div className="space-y-2">
                        {Object.entries(serverHealth.apiCredentials).map(([api, status]) => (
                          <div key={api} className="flex items-center justify-between text-xs">
                            <span className="text-slate-400">{api}</span>
                            <Badge 
                              variant={status.includes('❌') ? 'destructive' : 'default'}
                              className="text-xs"
                            >
                              {status.includes('❌') ? '미설정' : '설정됨'}
                            </Badge>
                          </div>
                        ))}
                      </div>
                    </CardContent>
                  </Card>
                </div>
              )}

              {/* Navigation */}
              <div className="mt-auto p-4 space-y-2">
                <Button 
                  variant="ghost" 
                  className="w-full justify-start text-slate-300 hover:bg-white/10"
                >
                  <Activity className="h-4 w-4 mr-2" />
                  활동 로그
                </Button>
                <Button 
                  variant="ghost" 
                  className="w-full justify-start text-slate-300 hover:bg-white/10"
                >
                  <BarChart3 className="h-4 w-4 mr-2" />
                  메트릭
                </Button>
                <Button 
                  variant="ghost" 
                  className="w-full justify-start text-slate-300 hover:bg-white/10"
                >
                  <Settings className="h-4 w-4 mr-2" />
                  설정
                </Button>
              </div>
            </>
          )}

          {/* Collapsed State */}
          {isCollapsed && (
            <div className="flex flex-col items-center py-4 space-y-4">
              <Button
                variant="ghost"
                size="sm"
                className="text-slate-400 hover:text-white hover:bg-white/10"
                title="새로운 대화"
                onClick={onClearChat}
              >
                <Plus className="h-5 w-5" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="text-slate-400 hover:text-white hover:bg-white/10"
                title="서버 상태"
              >
                <Server className="h-5 w-5" />
              </Button>
              <Button
                variant="ghost"
                size="sm"
                className="text-slate-400 hover:text-white hover:bg-white/10"
                title="설정"
              >
                <Settings className="h-5 w-5" />
              </Button>
            </div>
          )}
        </div>
      </div>
    </>
  );
}