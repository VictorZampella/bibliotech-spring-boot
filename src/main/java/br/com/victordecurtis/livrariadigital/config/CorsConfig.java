package br.com.victordecurtis.livrariadigital.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("*")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Location", "Idempotent-Replayed", "Deprecation", "Sunset",
                                "X-Rate-Limit-Limit", "X-Rate-Limit-Remaining", "Retry-After", "X-API-Key",
                                "X-Rate-Limit-IP-Limit", "X-Rate-Limit-IP-Remaining",
                                "X-Rate-Limit-Key-Limit", "X-Rate-Limit-Key-Remaining")
                        .maxAge(3600);
            }
        };
    }
}
