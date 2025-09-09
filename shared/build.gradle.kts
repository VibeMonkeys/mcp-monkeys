import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
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
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("org.springframework.boot:spring-boot-starter-web:3.5.4")
    implementation("org.springframework:spring-context:6.2.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("io.micrometer:micrometer-core:1.11.5")
    implementation("org.slf4j:slf4j-api:2.0.9")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}