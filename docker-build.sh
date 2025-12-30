#!/bin/bash

# MCP Monkeys Docker Build Script

set -e

echo "🐵 MCP Monkeys Docker Build"
echo "=========================="

# 환경 변수 확인
if [ -z "$GOOGLE_CLOUD_PROJECT" ]; then
    echo "⚠️  GOOGLE_CLOUD_PROJECT 환경 변수가 설정되지 않았습니다."
    echo "   export GOOGLE_CLOUD_PROJECT=your-project-id"
fi

case "$1" in
    build)
        echo "🔨 Building all images..."
        docker-compose build
        ;;
    up)
        echo "🚀 Starting all services..."
        docker-compose up -d
        echo "✅ Services started. Check status with: docker-compose ps"
        ;;
    down)
        echo "🛑 Stopping all services..."
        docker-compose down
        ;;
    logs)
        service=${2:-""}
        if [ -z "$service" ]; then
            docker-compose logs -f
        else
            docker-compose logs -f "$service"
        fi
        ;;
    restart)
        echo "🔄 Restarting all services..."
        docker-compose down
        docker-compose up -d
        ;;
    status)
        docker-compose ps
        ;;
    clean)
        echo "🧹 Cleaning up..."
        docker-compose down -v --rmi local
        ;;
    *)
        echo "Usage: $0 {build|up|down|logs|restart|status|clean}"
        echo ""
        echo "Commands:"
        echo "  build   - Build all Docker images"
        echo "  up      - Start all services"
        echo "  down    - Stop all services"
        echo "  logs    - View logs (optionally specify service name)"
        echo "  restart - Restart all services"
        echo "  status  - Show service status"
        echo "  clean   - Remove containers, volumes, and images"
        exit 1
        ;;
esac
