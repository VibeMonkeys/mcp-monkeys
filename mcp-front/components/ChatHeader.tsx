import { Badge } from "./ui/badge";
import { Button } from "./ui/button";
import { Settings, RefreshCw } from "lucide-react";

interface ChatHeaderProps {
  isConnected: boolean;
  onReconnect: () => void;
  onSettings: () => void;
}

export function ChatHeader({ isConnected, onReconnect, onSettings }: ChatHeaderProps) {
  return (
    <div className="flex items-center justify-between p-4 border-b bg-card">
      <div className="flex items-center gap-3">
        <h1 className="text-lg font-medium">MCP Client</h1>
        <Badge variant={isConnected ? "default" : "destructive"}>
          {isConnected ? "연결됨" : "연결 끊김"}
        </Badge>
      </div>
      
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="sm"
          onClick={onReconnect}
          disabled={isConnected}
        >
          <RefreshCw className="h-4 w-4" />
        </Button>
        <Button
          variant="ghost"
          size="sm"
          onClick={onSettings}
        >
          <Settings className="h-4 w-4" />
        </Button>
      </div>
    </div>
  );
}