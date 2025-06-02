dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.postgresql:postgresql")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
}

// бест практис
configurations.all {
    resolutionStrategy.eachDependency {
        if (requested.group == "org.springdoc" && requested.name == "springdoc-openapi-starter-webflux-ui") {
            useTarget("org.springdoc:springdoc-openapi-starter-webmvc-ui:${requested.version}")
        }
    }
}