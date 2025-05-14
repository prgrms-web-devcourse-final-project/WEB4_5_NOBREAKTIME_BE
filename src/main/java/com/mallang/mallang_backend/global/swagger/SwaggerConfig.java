package com.mallang.mallang_backend.global.swagger;

import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
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
    public OpenApiCustomizer idempotencyKeyHeaderCustomizer() {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) -> {
                if ("/api/v1/payment/confirm".equals(path)) {
                    pathItem.readOperations().forEach(operation -> {
                        Parameter idempotencyKeyParam = new Parameter()
                                .name("Idempotency-key")
                                .description(
                                        "멱등성 토큰 (클라이언트에서 생성한 UUID)\n" +
                                                "- 생성 규칙:\n" +
                                                "  1. 결제 시작 시 1회 생성합니다. (예: `crypto.randomUUID()`)\n" +
                                                "  2. 재시도 시 동일한 키 사용합니다. (로컬 스토리지 보관)\n" +
                                                "  3. 새 결제 시 새 키를 생성합니다."
                                )
                                .required(true)
                                .schema(new StringSchema().example("550e8400-e29b-41d4-a716-446655440000"));

                        // 기존 파라미터에 추가 (기존 파라미터 유지)
                        operation.addParametersItem(idempotencyKeyParam);
                    });
                }
            });
        };
    }
}
