package com.example.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringDocServerCustomizer implements OpenApiCustomizer {

    @Value("${app.gateway.base-url-for-service:http://localhost:8080}")
    private String gatewayServerUrl;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Override
    public void customise(OpenAPI openApi) {
        Server server = new Server();
        server.setUrl(gatewayServerUrl);
        server.setDescription("API Gateway");

        openApi.setServers(List.of(server));
    }
}