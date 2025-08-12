package com.likelion.picklbe.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;

@Configuration
@OpenAPIDefinition(info = @Info(title = "PickL API", version = "v1", description = "PickL OpenAPI"))
public class SwaggerConfig {

  @Value("${server.servlet.context-path:}")
  private String contextPath;

  @Bean
  public OpenAPI customOpenAPI() {
    Server localServer =
        new Server()
            .url(contextPath) // ex) "" or "/api"
            .description("Local Server");

    return new OpenAPI()
        .addServersItem(localServer)
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .info(
            new io.swagger.v3.oas.models.info.Info()
                .title("Swagger API 명세서")
                .version("1.0")
                .description("My Swagger")
                .contact(new Contact().name("PickL")))
        .addTagsItem(new Tag().name("퀴즈 API").description("일일 O/X 퀴즈"))
        .addTagsItem(new Tag().name("포인트 API").description("포인트 지갑/거래"));
  }

  @Bean // 중복 방지: 이 Bean은 이 파일에만 존재
  public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/**") // 반드시 포함
        .packagesToScan("com.likelion.picklbe") // 컨트롤러 루트 패키지
        .build();
  }
}
