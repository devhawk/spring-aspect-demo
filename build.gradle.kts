plugins {
	java
	id("org.springframework.boot") version "4.0.3"
	id("com.diffplug.spotless") version "8.3.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

spotless {
	java {
		googleJavaFormat()
		importOrder("dev.dbos", "java", "javax", "")
		removeUnusedImports()
		trimTrailingWhitespace()
		endWithNewline()
	}
}

repositories {
	mavenLocal()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-jdbc")
	implementation("org.springframework.boot:spring-boot-starter-aspectj")
	implementation("org.jdbi:jdbi3-core:3.51.0")
	implementation("org.jdbi:jdbi3-sqlobject:3.51.0")
	implementation("org.postgresql:postgresql:42.7.10")
	implementation("dev.dbos:transact:0.8.0-a19-g0de7708")

	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
