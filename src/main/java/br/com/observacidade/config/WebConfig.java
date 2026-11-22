package br.com.observacidade.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Atalhos de navegação para o painel web.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/painel").setViewName("forward:/painel/index.html");
        registry.addViewController("/painel/").setViewName("forward:/painel/index.html");
    }
}
