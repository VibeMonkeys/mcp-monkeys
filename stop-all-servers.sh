#!/bin/bash

# MCP Monkeys - 모든 서버 종료 스크립트
# 사용법: ./stop-all-servers.sh

echo "🛑 MCP Monkeys 서버 종료 중..."
echo "======================================"

# Gradle 데몬과 Java 프로세스 종료
echo "🔍 실행 중인 MCP 서버 프로세스 검색 중..."

# MCP 서버 관련 Java 프로세스 찾기
WEATHER_PID=$(ps aux | grep "mcp-weather-server" | grep -v grep | awk '{print $2}')
NEWS_PID=$(ps aux | grep "mcp-news-server" | grep -v grep | awk '{print $2}')
TRANSLATE_PID=$(ps aux | grep "mcp-translate-server" | grep -v grep | awk '{print $2}')
CALENDAR_PID=$(ps aux | grep "mcp-calendar-server" | grep -v grep | awk '{print $2}')
CLIENT_PID=$(ps aux | grep "mcp-client" | grep -v grep | awk '{print $2}')

# 개별 서버 종료 함수
kill_server() {
    local pid=$1
    local name=$2
    
    if [[ -n "$pid" ]]; then
        echo "🔥 $name 종료 중... (PID: $pid)"
        kill $pid 2>/dev/null
        sleep 2
        # 강제 종료가 필요한 경우
        if kill -0 $pid 2>/dev/null; then
            echo "   강제 종료 중..."
            kill -9 $pid 2>/dev/null
        fi
        echo "   ✓ $name 종료 완료"
    else
        echo "   - $name: 실행 중이 아님"
    fi
}

# 각 서버 종료
kill_server "$WEATHER_PID" "Weather Server"
kill_server "$NEWS_PID" "News Server"  
kill_server "$TRANSLATE_PID" "Translate Server"
kill_server "$CALENDAR_PID" "Calendar Server"
kill_server "$CLIENT_PID" "MCP Client"

echo ""
echo "🧹 Gradle 데몬 정리 중..."

# 모든 Gradle 데몬 종료
./gradlew --stop > /dev/null 2>&1

# bootRun 관련 프로세스들 정리
echo "🧹 남은 bootRun 프로세스 정리 중..."
BOOTRUN_PIDS=$(ps aux | grep "bootRun" | grep -v grep | awk '{print $2}')
for pid in $BOOTRUN_PIDS; do
    if [[ -n "$pid" ]]; then
        kill $pid 2>/dev/null
        echo "   bootRun 프로세스 종료: $pid"
    fi
done

# Java 프로세스 중 MCP 관련된 것들 정리
echo "🧹 MCP 관련 Java 프로세스 정리 중..."
MCP_JAVA_PIDS=$(ps aux | grep java | grep -E "(weather|news|translate|calendar|client)" | grep -v grep | awk '{print $2}')
for pid in $MCP_JAVA_PIDS; do
    if [[ -n "$pid" ]]; then
        kill $pid 2>/dev/null
        echo "   MCP Java 프로세스 종료: $pid"
    fi
done

# 포트 사용 상태 확인
echo ""
echo "🔍 포트 사용 상태 확인:"
echo "======================================"
check_port() {
    local port=$1
    local name=$2
    
    if lsof -i :$port > /dev/null 2>&1; then
        echo "⚠️  Port $port ($name): 여전히 사용 중"
        echo "   사용 중인 프로세스:"
        lsof -i :$port | grep LISTEN
    else
        echo "✅ Port $port ($name): 해제됨"
    fi
}

check_port "8090" "MCP Client"
check_port "8092" "Weather Server"
check_port "8093" "News Server"
check_port "8094" "Translate Server"
check_port "8095" "Calendar Server"

echo ""
echo "🧹 로그 파일 보관..."
if [ -d "logs" ]; then
    # 타임스탬프로 로그 백업
    TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
    mkdir -p "logs/archive"
    if ls logs/*.log 1> /dev/null 2>&1; then
        tar -czf "logs/archive/server_logs_$TIMESTAMP.tar.gz" logs/*.log
        echo "   로그 파일이 logs/archive/server_logs_$TIMESTAMP.tar.gz 에 보관되었습니다"
        rm -f logs/*.log
    fi
fi

echo ""
echo "✅ 모든 MCP 서버가 종료되었습니다!"
echo ""
echo "💡 팁:"
echo "• 서버 재시작: ./start-all-servers.sh"
echo "• 로그 확인: ls -la logs/archive/"