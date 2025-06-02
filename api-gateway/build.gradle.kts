plugins {
    id("io.spring.dependency-management") version "1.1.4"
}

val springCloudVersion = "2023.0.1"

dependencies {
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:2.5.0")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway")
    //configurations.implementation.exclude(group: "org.springframework.boot", module: "spring-boot-starter-web")
    //configurations.implementation.exclude(group: "org.springdoc", module: "springdoc-openapi-starter-webmvc-ui")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    enabled = true
}