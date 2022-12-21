import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion by extra("1.6.21") // Sync with plugins
val springBootVersion by extra("2.7.3") // Sync with plugins
val kotlinLoggingVersion by extra("2.1.23")
val springfoxBootStarterVersion by extra("3.0.0")
val hibernateTypesVersion by extra("2.19.2")
val springDocOpenApiVersion by extra("1.6.14")

plugins {
	val kotlinVersion = "1.6.21" // Sync with top-level declaration
	val springBootVersion = "2.7.3" // Sync with top-level declaration
	val springDependencyManagementVersion = "1.0.13.RELEASE"

	kotlin("jvm") version kotlinVersion
	kotlin("plugin.spring") version kotlinVersion apply false
	kotlin("plugin.jpa") version kotlinVersion apply false
	id("io.spring.dependency-management") version springDependencyManagementVersion apply false
	id("org.springframework.boot") version springBootVersion apply false
}

allprojects {
	group = "ru.sui.bi"
	version = "0.0.1"

	repositories {
		mavenCentral()
	}
}

subprojects {
	apply(plugin = "kotlin")
	apply(plugin = "io.spring.dependency-management")

	java.sourceCompatibility = JavaVersion.VERSION_17
	java.targetCompatibility = JavaVersion.VERSION_17

	tasks.withType<JavaCompile> {
		options.encoding = "UTF-8"
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

	configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
		imports {
			mavenBom("org.springframework.boot:spring-boot-dependencies:$springBootVersion")
		}
	}
}
