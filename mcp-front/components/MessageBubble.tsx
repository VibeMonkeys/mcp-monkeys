import { cn } from "./ui/utils";
import { Avatar, AvatarFallback } from "./ui/avatar";
import { Badge } from "./ui/badge";
import { User, Bot, CheckCheck, Clock, AlertCircle, Wrench } from "lucide-react";
import { motion } from "motion/react";

export interface Message {
  id: string;
  content: string;
  timestamp: Date;
  sender: 'user' | 'assistant';
  type: 'text' | 'error';
  status?: 'sending' | 'sent' | 'error';
  toolsUsed?: string[];
}

interface MessageBubbleProps {
  message: Message;
}

export function MessageBubble({ message }: MessageBubbleProps) {
  const isUser = message.sender === 'user';
  
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.3 }}
      className={cn(
        "flex gap-4 max-w-4xl mx-auto w-full mb-6",
        isUser ? "flex-row-reverse" : "flex-row"
      )}
    >
      {/* Avatar */}
      <div className="flex-shrink-0">
        <Avatar className="h-10 w-10 border-2 border-slate-200/20">
          <AvatarFallback className={cn(
            "text-sm font-medium",
            isUser 
              ? "bg-gradient-to-br from-blue-500 to-purple-600 text-white" 
              : "bg-gradient-to-br from-slate-600 to-slate-700 text-white"
          )}>
            {isUser ? <User className="h-5 w-5" /> : <Bot className="h-5 w-5" />}
          </AvatarFallback>
        </Avatar>
      </div>
      
      {/* Message Content */}
      <div className={cn(
        "flex flex-col gap-2 min-w-0 flex-1",
        isUser ? "items-end" : "items-start"
      )}>
        {/* Message Bubble */}
        <div className={cn(
          "relative rounded-2xl px-6 py-4 max-w-[85%] shadow-lg",
          isUser 
            ? "bg-gradient-to-br from-blue-500 to-purple-600 text-white ml-8" 
            : message.type === 'error'
              ? "bg-red-50 border border-red-200 text-red-800 mr-8"
              : "bg-white border border-slate-200/50 text-slate-800 mr-8"
        )}>
          {/* Glass effect for user messages */}
          {isUser && (
            <div className="absolute inset-0 rounded-2xl bg-white/10 backdrop-blur-sm" />
          )}
          
          {/* Content */}
          <div className="relative z-10">
            <p className="whitespace-pre-wrap leading-relaxed">{message.content}</p>
            
            {/* Tools Used */}
            {message.toolsUsed && message.toolsUsed.length > 0 && (
              <div className="mt-3 pt-3 border-t border-white/20">
                <div className="flex items-center gap-2 flex-wrap">
                  <Wrench className="h-3 w-3 opacity-70" />
                  <span className="text-xs opacity-70">사용된 도구:</span>
                  {message.toolsUsed.map((tool, index) => (
                    <Badge key={index} variant="secondary" className="text-xs px-2 py-0.5">
                      {tool}
                    </Badge>
                  ))}
                </div>
              </div>
            )}
          </div>
          
          {/* Message tail */}
          <div className={cn(
            "absolute w-3 h-3 transform rotate-45",
            isUser 
              ? "bg-gradient-to-br from-blue-500 to-purple-600 -right-1 bottom-4"
              : message.type === 'error'
                ? "bg-red-50 border-l border-b border-red-200 -left-1 bottom-4"
                : "bg-white border-l border-b border-slate-200/50 -left-1 bottom-4"
          )} />
        </div>
        
        {/* Message Meta */}
        <div className={cn(
          "flex items-center gap-2 text-xs",
          isUser ? "text-slate-400" : "text-slate-500"
        )}>
          <span>
            {message.timestamp.toLocaleTimeString('ko-KR', { 
              hour: '2-digit', 
              minute: '2-digit' 
            })}
          </span>
          
          {isUser && message.status && (
            <div className="flex items-center gap-1">
              {message.status === 'sending' && (
                <>
                  <Clock className="h-3 w-3" />
                  <span>전송중</span>
                </>
              )}
              {message.status === 'sent' && (
                <>
                  <CheckCheck className="h-3 w-3 text-blue-500" />
                  <span className="text-blue-500">전송됨</span>
                </>
              )}
              {message.status === 'error' && (
                <>
                  <AlertCircle className="h-3 w-3 text-red-500" />
                  <span className="text-red-500">전송 실패</span>
                </>
              )}
            </div>
          )}
        </div>
      </div>
    </motion.div>
  );
}