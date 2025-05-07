package com.mallang.mallang_backend.global.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        Info info = new Info()
                .version("v1.0")
                .title("API 명세서")
                .description("AI 기반 언어 학습 플랫폼 Mallang 의 API 문서입니다.")
                .contact(new Contact().name("7팀 - Mallang").email("teammallang@google.com"))
                .license(new License().name("Apache 2.0").url("http://springdoc.org"));

        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")
                .in(SecurityScheme.In.HEADER)
                .name("Authorization");

        SecurityRequirement securityRequirement = new SecurityRequirement().addList("bearerAuth");

        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .addServersItem(new Server()
                        .url("https://api.mallang.site")
                        .description("Production server"))
                .addServersItem(new Server()
                        .url("http://localhost:8080")
                        .description("Local server"))
                .components(new Components().addSecuritySchemes("bearerAuth", securityScheme));
    }

    @Bean
    public OpenApiCustomizer globalOperationDescriptionCustomizer() {
        return openApi -> openApi.getPaths().values().forEach(pathItem ->
                pathItem.readOperations().forEach(operation -> {
                    String originDesc = operation.getDescription() == null ? "" : operation.getDescription();
                    String commonDesc = "\n\n모든 응답은 공통 래퍼 객체(RsData)에 감싸져 반환됩니다.\n" +
                            "응답 예시:\n" +
                            "{\n" +
                            "  \"code\": \"200\",\n" +
                            "  \"message\": \"회원 정보가 수정되었습니다.\",\n" +
                            "  \"data\": {\n" +
                            "    \"email\": \"user@example.com\",\n" +
                            "    \"nickname\": \"cool_user\"\n" +
                            "  }\n" +
                            "}";
                    operation.setDescription(originDesc + commonDesc);
                })
        );
    }
}