plugins {
	kotlin("plugin.spring")
}

dependencies {
	api(project(":core"))
	api("org.springframework.boot:spring-boot-autoconfigure")
	api("org.springframework:spring-jdbc")
	api("com.zaxxer:HikariCP")
	api("org.postgresql:postgresql")
}