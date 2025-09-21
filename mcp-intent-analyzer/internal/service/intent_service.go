package service

import (
	"context"
	"time"

	"github.com/monkeys/mcp-intent-analyzer/internal/gemini"
	"github.com/sirupsen/logrus"
)

// IntentService 의도분석 서비스
type IntentService struct {
	geminiClient gemini.Client
	logger       *logrus.Logger
}

// NewIntentService 새로운 의도분석 서비스 생성
func NewIntentService(geminiClient gemini.Client, logger *logrus.Logger) *IntentService {
	return &IntentService{
		geminiClient: geminiClient,
		logger:       logger,
	}
}

// AnalyzeIntent 의도 분석 수행
func (s *IntentService) AnalyzeIntent(ctx context.Context, req *AnalyzeIntentRequest) (*AnalyzeIntentResponse, error) {
	startTime := time.Now()
	
	s.logger.WithFields(logrus.Fields{
		"text":    req.Text,
		"domain":  req.Domain,
		"user_id": req.UserID,
	}).Info("Starting intent analysis")

	// Gemini 클라이언트를 통한 의도 분석
	geminiRequest := &gemini.AnalyzeRequest{
		Text:            req.Text,
		Domain:          req.Domain,
		UserID:          req.UserID,
		ContextMessages: req.ContextMessages,
		Metadata:        req.Metadata,
	}

	geminiResponse, err := s.geminiClient.AnalyzeIntent(ctx, geminiRequest)
	if err != nil {
		s.logger.WithError(err).Error("Gemini analysis failed")
		return nil, err
	}

	// 응답 변환
	response := &AnalyzeIntentResponse{
		IntentType:           geminiResponse.IntentType,
		DomainSpecificIntent: geminiResponse.DomainSpecificIntent,
		Keywords:             convertKeywords(geminiResponse.Keywords),
		Priority:             convertPriority(geminiResponse.Priority),
		Confidence:           geminiResponse.Confidence,
		EmotionalTone:        convertEmotionalTone(geminiResponse.EmotionalTone),
		IntentScores:         geminiResponse.IntentScores,
		Reasoning:            geminiResponse.Reasoning,
		Metrics: &ProcessingMetrics{
			ProcessingTimeMs: time.Since(startTime).Milliseconds(),
			GeminiAPITimeMs:  geminiResponse.ProcessingTimeMs,
			CacheHit:         false, // 나중에 캐시 구현시 업데이트
			ModelVersion:     "gemini-1.5-pro-001",
		},
	}

	s.logger.WithFields(logrus.Fields{
		"intent":           response.IntentType,
		"confidence":       response.Confidence,
		"priority":         response.Priority,
		"processing_time":  response.Metrics.ProcessingTimeMs,
	}).Info("Intent analysis completed")

	return response, nil
}

// IsHealthy 서비스 상태 확인
func (s *IntentService) IsHealthy(ctx context.Context) bool {
	return s.geminiClient.IsHealthy(ctx)
}

// DTO 정의들
type AnalyzeIntentRequest struct {
	Text            string            `json:"text"`
	Domain          string            `json:"domain"`
	UserID          string            `json:"user_id"`
	SessionID       string            `json:"session_id"`
	ContextMessages []string          `json:"context_messages"`
	Metadata        map[string]string `json:"metadata"`
	Timestamp       time.Time         `json:"timestamp"`
}

type AnalyzeIntentResponse struct {
	IntentType           string                 `json:"intent_type"`
	DomainSpecificIntent string                 `json:"domain_specific_intent"`
	Keywords             []*Keyword             `json:"keywords"`
	Confidence           float64                `json:"confidence"`
	Priority             Priority               `json:"priority"`
	EmotionalTone        EmotionalTone          `json:"emotional_tone"`
	IntentScores         map[string]float64     `json:"intent_scores"`
	Reasoning            string                 `json:"reasoning"`
	Metrics              *ProcessingMetrics     `json:"metrics"`
}

type Keyword struct {
	Text     string  `json:"text"`
	Weight   float64 `json:"weight"`
	Category string  `json:"category"`
}

type ProcessingMetrics struct {
	ProcessingTimeMs int64  `json:"processing_time_ms"`
	GeminiAPITimeMs  int64  `json:"gemini_api_time_ms"`
	CacheHitCount    int64  `json:"cache_hit_count"`
	ModelVersion     string `json:"model_version"`
	CacheHit         bool   `json:"cache_hit"`
}

// Enums
type Priority int32

const (
	PriorityUnspecified Priority = iota
	PriorityLow
	PriorityMedium
	PriorityHigh
	PriorityUrgent
	PriorityCritical
)

func (p Priority) String() string {
	switch p {
	case PriorityLow:
		return "P4"
	case PriorityMedium:
		return "P3"
	case PriorityHigh:
		return "P2"
	case PriorityUrgent:
		return "P1"
	case PriorityCritical:
		return "P0"
	default:
		return "P3"
	}
}

type EmotionalTone int32

const (
	ToneNeutral EmotionalTone = iota
	TonePositive
	ToneNegative
	ToneFrustrated
	ToneUrgent
	ToneGrateful
)

func (t EmotionalTone) String() string {
	switch t {
	case TonePositive:
		return "positive"
	case ToneNegative:
		return "negative"
	case ToneFrustrated:
		return "frustrated"
	case ToneUrgent:
		return "urgent"
	case ToneGrateful:
		return "grateful"
	default:
		return "neutral"
	}
}

// 변환 헬퍼 함수들
func convertKeywords(geminiKeywords []gemini.Keyword) []*Keyword {
	keywords := make([]*Keyword, len(geminiKeywords))
	for i, k := range geminiKeywords {
		keywords[i] = &Keyword{
			Text:     k.Text,
			Weight:   k.Weight,
			Category: k.Category,
		}
	}
	return keywords
}

func convertPriority(priorityStr string) Priority {
	switch priorityStr {
	case "P0":
		return PriorityCritical
	case "P1":
		return PriorityUrgent
	case "P2":
		return PriorityHigh
	case "P3":
		return PriorityMedium
	case "P4":
		return PriorityLow
	default:
		return PriorityMedium
	}
}

func convertEmotionalTone(toneStr string) EmotionalTone {
	switch toneStr {
	case "positive":
		return TonePositive
	case "negative":
		return ToneNegative
	case "frustrated":
		return ToneFrustrated
	case "urgent":
		return ToneUrgent
	case "grateful":
		return ToneGrateful
	default:
		return ToneNeutral
	}
}