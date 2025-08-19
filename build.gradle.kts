import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.0"
    id("nebula.release") version "19.0.10"
    id("java")
    id("idea")
    kotlin("jvm") version "1.6.21"
    kotlin("plugin.spring") version "1.6.21"
    kotlin("plugin.allopen") version "1.6.21"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("org.jlleitschuh.gradle.ktlint") version "11.0.0"
    id("org.jetbrains.kotlin.kapt") version "1.6.21"
}

group = "com.mjg.cp.connector.mindtree"
description = "MindTree Connector Service - public interface"

java.sourceCompatibility = JavaVersion.VERSION_11

extra.apply {
    set("buildTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm")))
    set("log4j2.version", "2.17.0")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

extra["springCloudVersion"] = "2021.0.3"
extra["spring-metrics.version"] = "2.0.11"
extra["log4j2.version"] = "2.17.0"

dependencies {

    // Spring

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-starter-sleuth")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    runtimeOnly("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Spring Test Framework

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")

    // JetBrains

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Jackson

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.7")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.7")

    // Company

    implementation("com.mjg.common:mjg-mvc-exception:2.0.4")
    implementation("com.mjg.cs.common:kotlin-test-utils:0.1.19")
    implementation("com.mjg.cs.common:mjg-cs-springboot-starter:0.0.83")
    implementation("com.mjg.common:mjg-header-forwarding-starter:2.1.21")

    // Miscellaneous

    implementation("org.apache.httpcomponents:httpclient")

    // MindTree

    implementation("com.mindtreeepayments.gateway:mindtree-java:3.40.0")

    // Swagger

    implementation("org.springdoc:springdoc-openapi-ui:1.8.0")

    // DynaTrace

    implementation("com.mjg.common:company-dynatrace-metrics-starter:0.0.5")
    implementation("com.ryantenney.metrics:metrics-spring:3.1.3")

    // Testing

    testImplementation("com.github.tomakehurst:wiremock-standalone:2.27.2")
    testImplementation("io.mockk:mockk:1.12.3")
    testImplementation("net.bytebuddy:byte-buddy:1.14.2")
    testImplementation("org.amshove.kluent:kluent-android:1.46")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")

    // JUnit Jupiter API and TestEngine implementation

    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.0-M1")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.8.0-M1")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

springBoot {
    buildInfo()
}

tasks {
    test {
        systemProperty("spring.profiles.active", System.getProperty("spring.profiles.active"))
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

tasks.jar {
    enabled = false
}

tasks.bootRun {
    systemProperties = System.getProperties().map { p -> Pair(p.key as String, p.value) }.toMap()
}

version = System.getenv("BUILD_VERSION")
