import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("java-library")
}

group = "com.monkeys"
version = "0.0.1-SNAPSHOT"
description = "MCP Shared DTOs"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.3")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.20")
    implementation("org.springframework.boot:spring-boot-starter-web:4.0.1")
    implementation("org.springframework:spring-context:7.0.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.micrometer:micrometer-core:1.15.0")
    implementation("org.slf4j:slf4j-api:2.0.16")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}