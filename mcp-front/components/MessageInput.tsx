import { useState, KeyboardEvent } from "react";
import { Button } from "./ui/button";
import { Textarea } from "./ui/textarea";
import { Send, StopCircle, Paperclip, Mic } from "lucide-react";
import { motion } from "motion/react";

interface MessageInputProps {
  onSendMessage: (content: string) => void;
  disabled?: boolean;
  isProcessing?: boolean;
  onStopProcessing?: () => void;
}

export function MessageInput({ 
  onSendMessage, 
  disabled = false, 
  isProcessing = false,
  onStopProcessing 
}: MessageInputProps) {
  const [message, setMessage] = useState("");

  const handleSend = () => {
    if (message.trim() && !disabled && !isProcessing) {
      onSendMessage(message.trim());
      setMessage("");
    }
  };

  const handleKeyPress = (e: KeyboardEvent<HTMLTextAreaElement>) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  };

  return (
    <div className="border-t border-slate-200/50 bg-white/80 backdrop-blur-sm">
      <div className="max-w-4xl mx-auto p-6">
        <div className="relative">
          {/* Input Container */}
          <div className="relative bg-white rounded-2xl shadow-lg border border-slate-200/50 overflow-hidden">
            <Textarea
              value={message}
              onChange={(e) => setMessage(e.target.value)}
              onKeyPress={handleKeyPress}
              placeholder="메시지를 입력하세요..."
              disabled={disabled}
              className="min-h-[60px] max-h-[120px] resize-none border-0 bg-transparent px-6 py-4 pr-32 text-slate-800 placeholder:text-slate-400 focus:ring-0 focus:outline-none"
            />
            
            {/* Action Buttons */}
            <div className="absolute right-3 bottom-3 flex items-center gap-2">
              <Button
                variant="ghost"
                size="sm"
                className="h-8 w-8 p-0 text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                <Paperclip className="h-4 w-4" />
              </Button>
              
              <Button
                variant="ghost"
                size="sm"
                className="h-8 w-8 p-0 text-slate-400 hover:text-slate-600 hover:bg-slate-100"
              >
                <Mic className="h-4 w-4" />
              </Button>
              
              {isProcessing ? (
                <Button
                  onClick={onStopProcessing}
                  variant="destructive"
                  size="sm"
                  className="h-8 w-8 p-0 bg-red-500 hover:bg-red-600"
                >
                  <StopCircle className="h-4 w-4" />
                </Button>
              ) : (
                <motion.div
                  whileHover={{ scale: 1.05 }}
                  whileTap={{ scale: 0.95 }}
                >
                  <Button
                    onClick={handleSend}
                    disabled={disabled || !message.trim()}
                    size="sm"
                    className="h-8 w-8 p-0 bg-gradient-to-r from-blue-500 to-purple-600 hover:from-blue-600 hover:to-purple-700 border-0 shadow-md disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <Send className="h-4 w-4" />
                  </Button>
                </motion.div>
              )}
            </div>
          </div>
          
          {/* Typing Indicator */}
          {message.length > 0 && (
            <motion.div
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              className="absolute -top-8 left-0 text-xs text-slate-400"
            >
              Enter로 전송, Shift+Enter로 줄바꿈
            </motion.div>
          )}
        </div>
        
        {/* Quick Actions */}
        <div className="flex items-center gap-2 mt-4">
          <div className="flex gap-2">
            <Button
              variant="outline"
              size="sm"
              className="text-xs bg-white/50 border-slate-200/50 text-slate-600 hover:bg-white hover:text-slate-800"
            >
              연결 상태 확인
            </Button>
            <Button
              variant="outline"
              size="sm"
              className="text-xs bg-white/50 border-slate-200/50 text-slate-600 hover:bg-white hover:text-slate-800"
            >
              서버 정보
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}