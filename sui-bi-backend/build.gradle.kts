import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinLoggingVersion by extra("2.1.23")
val springfoxBootStarterVersion by extra("3.0.0")
val zekoSqlBuilderVersion by extra("1.4.0")
val hibernateTypesVersion by extra("2.19.2")

plugins {
	id("org.springframework.boot") version "2.7.3"
	id("io.spring.dependency-management") version "1.0.13.RELEASE"
	kotlin("jvm") version "1.6.21"
	kotlin("plugin.spring") version "1.6.21"
	kotlin("plugin.jpa") version "1.6.21"
}

group = "ru.sui.bi"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("io.springfox:springfox-boot-starter:$springfoxBootStarterVersion")
	implementation("org.postgresql:postgresql")
	implementation("com.vladmihalcea:hibernate-types-55:$hibernateTypesVersion")
	implementation("io.zeko:zeko-sql-builder:$zekoSqlBuilderVersion")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = JavaVersion.VERSION_17.toString()
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
