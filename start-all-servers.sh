#!/bin/bash

# MCP Monkeys - 모든 서버 시작 스크립트
# 사용법: ./start-all-servers.sh

echo "🚀 MCP Monkeys 서버 시작 중..."
echo "======================================"

# 배경 프로세스를 추적하기 위한 배열
declare -a PIDS=()
declare -a SERVERS=()

# 서버 시작 함수
start_server() {
    local module=$1
    local port=$2
    local name=$3
    
    echo "📡 $name 시작 중... (Port: $port) [MOCK MODE]"
    
    if [[ "$module" == "mcp-client" ]]; then
        # MCP Client는 특별한 환경변수 필요
        GOOGLE_CLOUD_PROJECT="gen-lang-client-0532718093" GOOGLE_CLOUD_LOCATION="asia-northeast1" \
        ./gradlew :$module:bootRun --args='--server.port='$port > logs/$module.log 2>&1 &
    else
        # MCP 서버들은 MOCK 프로필로 실행
        SPRING_PROFILES_ACTIVE=mock ./gradlew :$module:bootRun --args='--server.port='$port > logs/$module.log 2>&1 &
    fi
    
    local pid=$!
    PIDS+=($pid)
    SERVERS+=("$name:$port")
    echo "   ✓ PID: $pid"
}

# 로그 디렉토리 생성
mkdir -p logs

# 기존 로그 파일 정리
rm -f logs/*.log

echo ""
echo "🔧 서버들을 백그라운드에서 시작합니다..."
echo ""

# 각 서버 시작 (의존성 순서 고려)
start_server "mcp-weather-server" "8092" "Weather Server"
sleep 2

start_server "mcp-news-server" "8093" "News Server"
sleep 2

start_server "mcp-translate-server" "8094" "Translate Server"
sleep 2

start_server "mcp-calendar-server" "8095" "Calendar Server"
sleep 2

start_server "mcp-client" "8090" "MCP Client"

echo ""
echo "⏳ 서버 시작 대기 중... (30초)"
sleep 30

echo ""
echo "🔍 서버 상태 확인 중..."
echo "======================================"

# 각 서버 헬스체크
check_server() {
    local url=$1
    local name=$2
    
    if curl -s "$url" > /dev/null 2>&1; then
        echo "✅ $name: 정상"
    else
        echo "❌ $name: 응답 없음"
    fi
}

check_server "http://localhost:8092/actuator/health" "Weather Server (8092)"
check_server "http://localhost:8093/actuator/health" "News Server (8093)"
check_server "http://localhost:8094/actuator/health" "Translate Server (8094)"
check_server "http://localhost:8095/actuator/health" "Calendar Server (8095)"
check_server "http://localhost:8090/actuator/health" "MCP Client (8090)"

echo ""
echo "🌐 MCP Client 종합 헬스체크:"
if curl -s "http://localhost:8090/api/health/mcp-servers" | jq -r '.overallStatus' 2>/dev/null | grep -q "UP"; then
    echo "✅ 모든 MCP 서버 통신 정상"
else
    echo "⚠️  일부 서버 통신 문제 발생"
fi

echo ""
echo "📊 실행 중인 프로세스:"
echo "======================================"
for i in "${!PIDS[@]}"; do
    pid=${PIDS[$i]}
    server=${SERVERS[$i]}
    if kill -0 $pid 2>/dev/null; then
        echo "✓ $server (PID: $pid)"
    else
        echo "✗ $server (종료됨)"
    fi
done

echo ""
echo "📋 유용한 명령어들:"
echo "======================================"
echo "• 로그 확인:"
echo "  tail -f logs/mcp-weather-server.log"
echo "  tail -f logs/mcp-news-server.log"
echo "  tail -f logs/mcp-translate-server.log"
echo "  tail -f logs/mcp-calendar-server.log"
echo "  tail -f logs/mcp-client.log"
echo ""
echo "• 헬스체크:"
echo "  curl http://localhost:8090/api/health/comprehensive | jq"
echo ""
echo "• 모든 서버 종료:"
echo "  ./stop-all-servers.sh"
echo ""
echo "🎉 모든 서버가 시작되었습니다!"
echo "MCP Client: http://localhost:8090"
echo ""

# 사용자가 스크립트를 종료할 때 모든 서버도 종료하는 옵션
read -p "Enter 키를 누르면 모든 서버를 종료합니다 (또는 Ctrl+C로 서버 유지)..." 
echo "서버들을 종료합니다..."
for pid in "${PIDS[@]}"; do
    if kill -0 $pid 2>/dev/null; then
        kill $pid
        echo "PID $pid 종료"
    fi
done
echo "모든 서버가 종료되었습니다."