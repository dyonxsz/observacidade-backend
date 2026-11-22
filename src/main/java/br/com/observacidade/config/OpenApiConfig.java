package br.com.observacidade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI observaCidadeOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Observa Cidade API")
                        .description("API de registro e acompanhamento de ocorrências urbanas. "
                                + "Faça login em /auth/login, copie o token e clique em Authorize para testar as rotas protegidas.")
                        .version("1.0.0")
                        .contact(new Contact().name("Observa Cidade")))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
