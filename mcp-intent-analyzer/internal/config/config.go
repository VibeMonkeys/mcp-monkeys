package config

import (
	"os"
	"strconv"
	"time"

	"github.com/sirupsen/logrus"
)

// Config 애플리케이션 설정
type Config struct {
	// Server 설정
	Server ServerConfig

	// Gemini 설정
	Gemini GeminiConfig

	// Redis 설정
	Redis RedisConfig

	// Logging 설정
	Log LogConfig
}

// ServerConfig gRPC 서버 설정
type ServerConfig struct {
	Port         string
	ReadTimeout  time.Duration
	WriteTimeout time.Duration
}

// GeminiConfig Gemini API 설정
type GeminiConfig struct {
	ProjectID string
	Location  string
	ModelName string
	APIKey    string
}

// RedisConfig Redis 설정
type RedisConfig struct {
	Address  string
	Password string
	DB       int
	TTL      time.Duration
}

// LogConfig 로깅 설정
type LogConfig struct {
	Level  string
	Format string
}

// Load 환경변수에서 설정 로드
func Load() *Config {
	return &Config{
		Server: ServerConfig{
			Port:         getEnv("SERVER_PORT", "8097"),
			ReadTimeout:  getDuration("SERVER_READ_TIMEOUT", 30*time.Second),
			WriteTimeout: getDuration("SERVER_WRITE_TIMEOUT", 30*time.Second),
		},
		Gemini: GeminiConfig{
			ProjectID: getEnv("GEMINI_PROJECT_ID", ""),
			Location:  getEnv("GEMINI_LOCATION", "us-central1"),
			ModelName: getEnv("GEMINI_MODEL_NAME", "gemini-1.5-pro-001"),
			APIKey:    getEnv("GEMINI_API_KEY", ""),
		},
		Redis: RedisConfig{
			Address:  getEnv("REDIS_ADDRESS", "localhost:6379"),
			Password: getEnv("REDIS_PASSWORD", ""),
			DB:       getEnvInt("REDIS_DB", 0),
			TTL:      getDuration("REDIS_TTL", 24*time.Hour),
		},
		Log: LogConfig{
			Level:  getEnv("LOG_LEVEL", "info"),
			Format: getEnv("LOG_FORMAT", "json"),
		},
	}
}

// Validate 설정값 검증
func (c *Config) Validate() error {
	if c.Gemini.ProjectID == "" {
		logrus.Warn("GEMINI_PROJECT_ID is not set, using mock mode")
	}
	if c.Gemini.APIKey == "" {
		logrus.Warn("GEMINI_API_KEY is not set, using mock mode")
	}
	return nil
}

// 헬퍼 함수들
func getEnv(key, defaultValue string) string {
	if value := os.Getenv(key); value != "" {
		return value
	}
	return defaultValue
}

func getEnvInt(key string, defaultValue int) int {
	if value := os.Getenv(key); value != "" {
		if intValue, err := strconv.Atoi(value); err == nil {
			return intValue
		}
	}
	return defaultValue
}

func getDuration(key string, defaultValue time.Duration) time.Duration {
	if value := os.Getenv(key); value != "" {
		if duration, err := time.ParseDuration(value); err == nil {
			return duration
		}
	}
	return defaultValue
}