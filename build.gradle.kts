plugins {
    id("java")
    id("org.springframework.boot") version "3.2.5" apply false
    id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
    group = "com.example"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        implementation("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        // Springdoc OpenAPI for Swagger

        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }


    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }
}