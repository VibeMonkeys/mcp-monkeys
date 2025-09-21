package gemini

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"
	"time"

	"google.golang.org/genai"
	"github.com/sirupsen/logrus"
)

// Client Vertex AI Gemini 클라이언트 인터페이스
type Client interface {
	AnalyzeIntent(ctx context.Context, request *AnalyzeRequest) (*AnalyzeResponse, error)
	IsHealthy(ctx context.Context) bool
}

// AnalyzeRequest 의도 분석 요청
type AnalyzeRequest struct {
	Text            string            `json:"text"`
	Domain          string            `json:"domain"`
	UserID          string            `json:"user_id"`
	ContextMessages []string          `json:"context_messages"`
	Metadata        map[string]string `json:"metadata"`
}

// AnalyzeResponse 의도 분석 응답
type AnalyzeResponse struct {
	IntentType           string             `json:"intent_type"`
	DomainSpecificIntent string             `json:"domain_specific_intent"`
	Keywords             []Keyword          `json:"keywords"`
	Priority             string             `json:"priority"`
	Confidence           float64            `json:"confidence"`
	EmotionalTone        string             `json:"emotional_tone"`
	UrgencyIndicators    []string           `json:"urgency_indicators"`
	Reasoning            string             `json:"reasoning"`
	IntentScores         map[string]float64 `json:"intent_scores"`
	ProcessingTimeMs     int64              `json:"processing_time_ms"`
}

// Keyword 키워드 정보
type Keyword struct {
	Text     string  `json:"text"`
	Weight   float64 `json:"weight"`
	Category string  `json:"category"`
}

// vertexAIClient Vertex AI를 사용한 실제 클라이언트
type vertexAIClient struct {
	projectID string
	location  string
	modelName string
	client    *genai.Client
	logger    *logrus.Logger
}

// NewClient Vertex AI Gemini 클라이언트 생성
func NewClient(projectID, location, modelName, apiKey string, logger *logrus.Logger) Client {
	if projectID == "" {
		logger.Fatal("GEMINI_PROJECT_ID is required for Vertex AI")
	}

	ctx := context.Background()
	client, err := genai.NewClient(ctx, &genai.ClientConfig{
		Project:  projectID,
		Location: location,
		Backend:  genai.BackendVertexAI,
	})
	if err != nil {
		logger.WithError(err).Fatal("Failed to create Vertex AI Gemini client")
	}

	logger.WithFields(logrus.Fields{
		"project_id": projectID,
		"location":   location,
		"model":      modelName,
	}).Info("Vertex AI Gemini client initialized successfully")

	return &vertexAIClient{
		projectID: projectID,
		location:  location,
		modelName: modelName,
		client:    client,
		logger:    logger,
	}
}

// AnalyzeIntent Vertex AI Gemini를 사용한 의도 분석
func (c *vertexAIClient) AnalyzeIntent(ctx context.Context, request *AnalyzeRequest) (*AnalyzeResponse, error) {
	startTime := time.Now()

	c.logger.WithFields(logrus.Fields{
		"text":   request.Text,
		"domain": request.Domain,
		"user":   request.UserID,
	}).Info("Analyzing intent with Vertex AI Gemini")

	// 프롬프트 생성
	prompt := c.buildPrompt(request)

	// Gemini API 호출
	parts := []*genai.Part{
		genai.NewPartFromText(prompt),
	}
	
	contents := []*genai.Content{
		{Parts: parts},
	}
	
	resp, err := c.client.Models.GenerateContent(ctx, c.modelName, contents, nil)
	if err != nil {
		c.logger.WithError(err).Error("Vertex AI Gemini generation failed")
		return nil, fmt.Errorf("vertex AI Gemini generation failed: %w", err)
	}

	// 응답 파싱
	response, err := c.parseGeminiResponse(resp, startTime)
	if err != nil {
		c.logger.WithError(err).Error("Failed to parse Gemini response")
		return nil, fmt.Errorf("failed to parse Gemini response: %w", err)
	}

	c.logger.WithFields(logrus.Fields{
		"intent":     response.IntentType,
		"confidence": response.Confidence,
		"duration":   response.ProcessingTimeMs,
	}).Info("Intent analysis completed with Vertex AI Gemini")

	return response, nil
}

// IsHealthy Vertex AI 연결 상태 확인
func (c *vertexAIClient) IsHealthy(ctx context.Context) bool {
	// 간단한 테스트 요청으로 헬스체크
	testRequest := &AnalyzeRequest{
		Text:   "test",
		Domain: "health_check",
	}

	_, err := c.AnalyzeIntent(ctx, testRequest)
	return err == nil
}

// buildPrompt 의도 분석용 프롬프트 생성
func (c *vertexAIClient) buildPrompt(request *AnalyzeRequest) string {
	contextStr := ""
	if len(request.ContextMessages) > 0 {
		contextStr = fmt.Sprintf("\n\n이전 대화 맥락:\n%s", strings.Join(request.ContextMessages, "\n"))
	}

	return fmt.Sprintf(`사용자의 메시지를 분석하여 의도, 우선순위, 감정 톤, 키워드를 파악해주세요.

분석할 메시지: "%s"
도메인: %s%s

다음 JSON 형식으로 정확히 응답해주세요:
{
  "intent_type": "question_how|question_what|request_help|report_issue|ask_status|question_general",
  "domain_specific_intent": "구체적인 도메인별 의도",
  "keywords": [
    {"text": "키워드", "weight": 0.9, "category": "technical|action|domain"}
  ],
  "priority": "P1|P2|P3|P4",
  "confidence": 0.85,
  "emotional_tone": "neutral|positive|negative|frustrated|urgent|grateful",
  "urgency_indicators": ["급해", "urgent"],
  "reasoning": "분석 근거",
  "intent_scores": {
    "question_how": 0.85,
    "question_what": 0.1,
    "request_help": 0.05
  }
}

분석 기준:
- intent_type: 주요 의도 (질문 방법, 질문 내용, 도움 요청, 문제 신고, 상태 문의, 일반 질문)
- priority: P1(긴급), P2(높음), P3(보통), P4(낮음)
- emotional_tone: 감정 상태
- keywords: 중요 키워드와 가중치
- confidence: 분석 신뢰도 (0.0-1.0)`, 
		request.Text, request.Domain, contextStr)
}

// parseGeminiResponse Gemini API 응답 파싱
func (c *vertexAIClient) parseGeminiResponse(resp *genai.GenerateContentResponse, startTime time.Time) (*AnalyzeResponse, error) {
	if resp == nil || len(resp.Candidates) == 0 {
		return nil, fmt.Errorf("no candidates in response")
	}

	candidate := resp.Candidates[0]
	if candidate.Content == nil || len(candidate.Content.Parts) == 0 {
		return nil, fmt.Errorf("no content parts in response")
	}

	// 첫 번째 텍스트 부분 가져오기
	var responseText string
	for _, part := range candidate.Content.Parts {
		if part.Text != "" {
			responseText = part.Text
			break
		}
	}

	if responseText == "" {
		return nil, fmt.Errorf("empty response text")
	}

	c.logger.WithField("raw_response", responseText).Debug("Raw Gemini response")

	// JSON 파싱
	var geminiResponse struct {
		IntentType           string             `json:"intent_type"`
		DomainSpecificIntent string             `json:"domain_specific_intent"`
		Keywords             []Keyword          `json:"keywords"`
		Priority             string             `json:"priority"`
		Confidence           float64            `json:"confidence"`
		EmotionalTone        string             `json:"emotional_tone"`
		UrgencyIndicators    []string           `json:"urgency_indicators"`
		Reasoning            string             `json:"reasoning"`
		IntentScores         map[string]float64 `json:"intent_scores"`
	}

	// JSON 부분만 추출 (```json ... ``` 형식인 경우)
	jsonStart := strings.Index(responseText, "{")
	jsonEnd := strings.LastIndex(responseText, "}")
	if jsonStart == -1 || jsonEnd == -1 {
		return nil, fmt.Errorf("no valid JSON found in response")
	}

	jsonText := responseText[jsonStart : jsonEnd+1]
	
	if err := json.Unmarshal([]byte(jsonText), &geminiResponse); err != nil {
		c.logger.WithFields(logrus.Fields{
			"json_text": jsonText,
			"error":     err,
		}).Error("Failed to parse JSON response")
		return nil, fmt.Errorf("failed to parse JSON response: %w", err)
	}

	return &AnalyzeResponse{
		IntentType:           geminiResponse.IntentType,
		DomainSpecificIntent: geminiResponse.DomainSpecificIntent,
		Keywords:             geminiResponse.Keywords,
		Priority:             geminiResponse.Priority,
		Confidence:           geminiResponse.Confidence,
		EmotionalTone:        geminiResponse.EmotionalTone,
		UrgencyIndicators:    geminiResponse.UrgencyIndicators,
		Reasoning:            geminiResponse.Reasoning,
		IntentScores:         geminiResponse.IntentScores,
		ProcessingTimeMs:     time.Since(startTime).Milliseconds(),
	}, nil
}