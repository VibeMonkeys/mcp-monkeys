package main

import (
	"context"
	"fmt"
	"net"
	"os"
	"os/signal"
	"syscall"
	"time"

	"github.com/monkeys/mcp-intent-analyzer/internal/config"
	"github.com/monkeys/mcp-intent-analyzer/internal/gemini"
	"github.com/monkeys/mcp-intent-analyzer/internal/handler"
	"github.com/monkeys/mcp-intent-analyzer/internal/service"
	intentpb "github.com/monkeys/mcp-intent-analyzer/proto/gen/proto/intent"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/reflection"
)

func main() {
	// 로거 설정
	logger := logrus.New()
	logger.SetFormatter(&logrus.JSONFormatter{})
	logger.SetLevel(logrus.InfoLevel)

	logger.Info("Starting Intent Analyzer Server...")

	// 설정 로드
	cfg := config.Load()
	if err := cfg.Validate(); err != nil {
		logger.WithError(err).Fatal("Configuration validation failed")
	}

	logger.WithFields(logrus.Fields{
		"port":       cfg.Server.Port,
		"log_level":  cfg.Log.Level,
		"model_name": cfg.Gemini.ModelName,
	}).Info("Configuration loaded successfully")

	// Gemini 클라이언트 생성
	geminiClient := gemini.NewClient(
		cfg.Gemini.ProjectID,
		cfg.Gemini.Location,
		cfg.Gemini.ModelName,
		cfg.Gemini.APIKey,
		logger,
	)

	// 서비스 생성
	intentService := service.NewIntentService(geminiClient, logger)

	// gRPC 핸들러 생성
	grpcHandler := handler.NewIntentAnalyzerServer(intentService, logger)

	// gRPC 서버 설정
	grpcServer := grpc.NewServer(
		grpc.UnaryInterceptor(loggingInterceptor(logger)),
	)

	// 서비스 등록
	intentpb.RegisterIntentAnalyzerServer(grpcServer, grpcHandler)

	// gRPC reflection 활성화 (개발/디버깅용)
	reflection.Register(grpcServer)

	// 서버 시작
	address := fmt.Sprintf(":%s", cfg.Server.Port)
	listener, err := net.Listen("tcp", address)
	if err != nil {
		logger.WithError(err).Fatal("Failed to listen on address")
	}

	logger.WithField("address", address).Info("gRPC server starting")

	// Graceful shutdown 설정
	go func() {
		if err := grpcServer.Serve(listener); err != nil {
			logger.WithError(err).Fatal("Failed to serve gRPC server")
		}
	}()

	// 종료 신호 대기
	quit := make(chan os.Signal, 1)
	signal.Notify(quit, syscall.SIGINT, syscall.SIGTERM)
	<-quit

	logger.Info("Shutting down server...")
	grpcServer.GracefulStop()
	logger.Info("Server stopped")
}

// loggingInterceptor gRPC 요청 로깅 미들웨어
func loggingInterceptor(logger *logrus.Logger) grpc.UnaryServerInterceptor {
	return func(ctx context.Context, req interface{}, info *grpc.UnaryServerInfo, handler grpc.UnaryHandler) (interface{}, error) {
		start := time.Now()
		
		logger.WithFields(logrus.Fields{
			"method": info.FullMethod,
		}).Info("gRPC request started")

		resp, err := handler(ctx, req)
		
		duration := time.Since(start)
		fields := logrus.Fields{
			"method":   info.FullMethod,
			"duration": duration.Milliseconds(),
		}
		
		if err != nil {
			fields["error"] = err.Error()
			logger.WithFields(fields).Error("gRPC request failed")
		} else {
			logger.WithFields(fields).Info("gRPC request completed")
		}

		return resp, err
	}
}

