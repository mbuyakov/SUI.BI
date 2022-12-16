val kotlinLoggingVersion: String by rootProject.extra
val hibernateTypesVersion: String by rootProject.extra

plugins {
	kotlin("plugin.spring")
	kotlin("plugin.jpa")
	id("org.springframework.boot")
}

dependencies {
	api(project(":engine:postgresql"))
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("com.vladmihalcea:hibernate-types-55:$hibernateTypesVersion")
	implementation("org.postgresql:postgresql")
	implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
}