package handler

import (
	"context"
	"time"

	"github.com/monkeys/mcp-intent-analyzer/internal/service"
	intentpb "github.com/monkeys/mcp-intent-analyzer/proto/gen/proto/intent"
	"github.com/sirupsen/logrus"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

// IntentAnalyzerServer gRPC 서버 구현
type IntentAnalyzerServer struct {
	intentpb.UnimplementedIntentAnalyzerServer
	intentService *service.IntentService
	logger        *logrus.Logger
}

// NewIntentAnalyzerServer 새로운 gRPC 서버 생성
func NewIntentAnalyzerServer(intentService *service.IntentService, logger *logrus.Logger) *IntentAnalyzerServer {
	return &IntentAnalyzerServer{
		intentService: intentService,
		logger:        logger,
	}
}

// AnalyzeIntent 의도 분석 gRPC 핸들러
func (s *IntentAnalyzerServer) AnalyzeIntent(ctx context.Context, req *intentpb.IntentRequest) (*intentpb.IntentResponse, error) {
	s.logger.WithFields(logrus.Fields{
		"method": "AnalyzeIntent",
		"text":   req.Text,
		"domain": req.Domain,
	}).Info("Received intent analysis request")

	// 입력 검증
	if req.Text == "" {
		return nil, status.Error(codes.InvalidArgument, "text field is required")
	}

	// 서비스 요청 변환
	serviceRequest := &service.AnalyzeIntentRequest{
		Text:            req.Text,
		Domain:          req.Domain,
		UserID:          req.UserId,
		SessionID:       req.SessionId,
		ContextMessages: req.ContextMessages,
		Metadata:        req.Metadata,
		Timestamp:       time.Now(),
	}

	// 의도 분석 수행
	serviceResponse, err := s.intentService.AnalyzeIntent(ctx, serviceRequest)
	if err != nil {
		s.logger.WithError(err).Error("Intent analysis failed")
		return nil, status.Error(codes.Internal, "intent analysis failed")
	}

	// gRPC 응답 변환
	response := &intentpb.IntentResponse{
		IntentType:           serviceResponse.IntentType,
		DomainSpecificIntent: serviceResponse.DomainSpecificIntent,
		Keywords:             convertToProtoKeywords(serviceResponse.Keywords),
		Confidence:           serviceResponse.Confidence,
		Priority:             convertToProtoPriority(serviceResponse.Priority),
		EmotionalTone:        convertToProtoEmotionalTone(serviceResponse.EmotionalTone),
		IntentScores:         serviceResponse.IntentScores,
		Reasoning:            serviceResponse.Reasoning,
		Metrics: &intentpb.ProcessingMetrics{
			ProcessingTimeMs: serviceResponse.Metrics.ProcessingTimeMs,
			GeminiApiTimeMs:  serviceResponse.Metrics.GeminiAPITimeMs,
			CacheHitCount:    serviceResponse.Metrics.CacheHitCount,
			ModelVersion:     serviceResponse.Metrics.ModelVersion,
			CacheHit:         serviceResponse.Metrics.CacheHit,
		},
	}

	s.logger.WithFields(logrus.Fields{
		"intent":     response.IntentType,
		"confidence": response.Confidence,
		"priority":   response.Priority.String(),
	}).Info("Intent analysis completed successfully")

	return response, nil
}

// HealthCheck 헬스체크 gRPC 핸들러
func (s *IntentAnalyzerServer) HealthCheck(ctx context.Context, req *intentpb.HealthCheckRequest) (*intentpb.HealthCheckResponse, error) {
	s.logger.Info("Health check requested")

	// 서비스 상태 확인
	isHealthy := s.intentService.IsHealthy(ctx)
	
	var status intentpb.HealthCheckResponse_ServingStatus
	var message string
	
	if isHealthy {
		status = intentpb.HealthCheckResponse_SERVING
		message = "Service is healthy"
	} else {
		status = intentpb.HealthCheckResponse_NOT_SERVING
		message = "Service is unhealthy"
	}

	response := &intentpb.HealthCheckResponse{
		Status:  status,
		Message: message,
		Details: map[string]string{
			"timestamp":     time.Now().Format(time.RFC3339),
			"service":       "intent-analyzer",
			"version":       "1.0.0",
		},
	}

	s.logger.WithFields(logrus.Fields{
		"status":  status.String(),
		"healthy": isHealthy,
	}).Info("Health check completed")

	return response, nil
}

// 변환 헬퍼 함수들
func convertToProtoKeywords(keywords []*service.Keyword) []*intentpb.Keyword {
	protoKeywords := make([]*intentpb.Keyword, len(keywords))
	for i, k := range keywords {
		protoKeywords[i] = &intentpb.Keyword{
			Text:     k.Text,
			Weight:   k.Weight,
			Category: k.Category,
		}
	}
	return protoKeywords
}

func convertToProtoPriority(priority service.Priority) intentpb.Priority {
	switch priority {
	case service.PriorityLow:
		return intentpb.Priority_PRIORITY_LOW
	case service.PriorityMedium:
		return intentpb.Priority_PRIORITY_MEDIUM
	case service.PriorityHigh:
		return intentpb.Priority_PRIORITY_HIGH
	case service.PriorityUrgent:
		return intentpb.Priority_PRIORITY_URGENT
	case service.PriorityCritical:
		return intentpb.Priority_PRIORITY_CRITICAL
	default:
		return intentpb.Priority_PRIORITY_MEDIUM
	}
}

func convertToProtoEmotionalTone(tone service.EmotionalTone) intentpb.EmotionalTone {
	switch tone {
	case service.TonePositive:
		return intentpb.EmotionalTone_TONE_POSITIVE
	case service.ToneNegative:
		return intentpb.EmotionalTone_TONE_NEGATIVE
	case service.ToneFrustrated:
		return intentpb.EmotionalTone_TONE_FRUSTRATED
	case service.ToneUrgent:
		return intentpb.EmotionalTone_TONE_URGENT
	case service.ToneGrateful:
		return intentpb.EmotionalTone_TONE_GRATEFUL
	default:
		return intentpb.EmotionalTone_TONE_NEUTRAL
	}
}