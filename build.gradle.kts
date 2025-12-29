plugins {
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.spring") version "2.2.21" apply false
    id("org.springframework.boot") version "4.0.1" apply false
    id("io.spring.dependency-management") version "1.1.7" apply false
}

allprojects {
    group = "com.monkeys"
    version = "0.0.1-SNAPSHOT"
    
    repositories {
        mavenCentral()
    }
}

// 서브프로젝트별로 개별 설정