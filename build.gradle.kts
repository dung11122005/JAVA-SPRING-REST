plugins {
	java
	id("org.springframework.boot") version "3.2.4"
	id("io.spring.dependency-management") version "1.1.4"
	id("io.freefair.lombok") version "8.6"
}

group = "vn.tandung"
version = "0.0.1-SNAPSHOT"

java {
	sourceCompatibility = JavaVersion.VERSION_17
}

repositories {
	mavenCentral()
}

// 1. Quản lý tập trung phiên bản AWS SDK (BOM)
dependencyManagement {
	imports {
		mavenBom("software.amazon.awssdk:bom:2.27.21")
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.thymeleaf.extras:thymeleaf-extras-springsecurity6")
	implementation("com.turkraft.springfilter:jpa:3.1.7")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")

	// 2. KHO THƯ VIỆN AWS (Không cần ghi version nhờ có BOM ở trên)
	implementation("software.amazon.awssdk:s3")        // Dành cho S3 (Lưu trữ file)
	implementation("software.amazon.awssdk:sqs")       // Dành cho SQS (Hàng đợi tin nhắn)
	implementation("software.amazon.awssdk:dynamodb")  // Dành cho DynamoDB (NoSQL)
	implementation("software.amazon.awssdk:auth")
	// thư viện hỗ trợ xử lý thông tin video và HLS
	implementation("ws.schild:jave-core:3.3.1")
	implementation("software.amazon.awssdk:cloudfront") 

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	implementation("org.springframework.boot:spring-boot-starter-mail")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
