#!/bin/bash

# gRPC 코드 생성 스크립트

set -e

# 프로젝트 루트로 이동
cd "$(dirname "$0")/.."

# proto 디렉토리가 있는지 확인
if [ ! -d "proto/intent" ]; then
    echo "Error: proto/intent directory not found"
    exit 1
fi

# 생성된 파일들을 저장할 디렉토리 생성
mkdir -p proto/gen/intent

echo "Generating gRPC code from protobuf..."

# protoc 실행
protoc \
    --go_out=proto/gen \
    --go_opt=paths=source_relative \
    --go-grpc_out=proto/gen \
    --go-grpc_opt=paths=source_relative \
    proto/intent/*.proto

echo "✅ gRPC code generation completed!"
echo "Generated files:"
find proto/gen -name "*.go" -type f