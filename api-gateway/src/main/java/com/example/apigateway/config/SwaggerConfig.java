package com.example.apigateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.util.ArrayList;
import java.util.List;

// вообще это крутой способ для конфигурации сваггера и обнаружения групп, но у меня это щас не используется, потому что
// локально удобно просто через property
@Configuration
public class SwaggerConfig {

    @Bean
    @Lazy(false)
    public List<GroupedOpenApi> apis(RouteDefinitionLocator locator) {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        if (definitions != null) {
            definitions.stream()
                    .filter(routeDefinition -> routeDefinition.getId().matches(".*-service-route"))
                    .forEach(routeDefinition -> {
                        String name = routeDefinition.getId().replace("-service-route", "");
                    });
        }
        return groups;
    }
}