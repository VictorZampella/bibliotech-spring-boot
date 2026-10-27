package br.com.victordecurtis.livrariadigital.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "BiblioTech",
                version = "1.0",
                description = "API BiblioTech para leitores, catálogo de livros, autores e categorias.",
                contact = @Contact(
                        name = "Victor de Curtis",
                        email = "victorcurtiss@outlook.com",
                        url = "https://meu-portfolio-five-neon.vercel.app/"
                ),
                termsOfService = "http://localhost:8080/termos-de-uso.html"
        ),
        servers = @Server(
                description = "Ambiente local",
                url = "http://localhost:8080"
        ))
public class OpenApiConfig {
}

