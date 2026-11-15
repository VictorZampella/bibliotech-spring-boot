package br.com.victordecurtis.livrariadigital.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
import org.springdoc.webmvc.ui.SwaggerIndexPageTransformer;
import org.springdoc.webmvc.ui.SwaggerIndexTransformer;
import org.springdoc.webmvc.ui.SwaggerWelcomeCommon;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;
import org.springframework.web.servlet.resource.ResourceTransformerChain;
import org.springframework.web.servlet.resource.TransformedResource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BiblioTechSwaggerIndexPageTransformer extends SwaggerIndexPageTransformer {
    private static final Pattern PADRAO_TAG_CORPO = Pattern.compile("(?i)<body[^>]*>");
    private static final String BANNER_BIBLIOTECH = """
            <header class="bibliotech-banner" aria-label="BiblioTech">
              <div class="bibliotech-banner__content">
                <div class="bibliotech-banner__brand"><span class="bibliotech-banner__mark">B</span><span><strong>BiblioTech</strong><small>Documentação interativa da API</small></span></div>
                <nav class="bibliotech-banner__links" aria-label="Links da BiblioTech">
                  <a href="https://meu-portfolio-five-neon.vercel.app/" target="_blank" rel="noopener noreferrer">Website</a>
                  <a href="mailto:victorcurtiss@outlook.com">Contato</a>
                  <a href="/termos-de-uso.html">Termos de uso</a>
                </nav>
              </div>
            </header>
            """;

    public BiblioTechSwaggerIndexPageTransformer(
            SwaggerUiConfigProperties swaggerUiConfig,
            SwaggerUiOAuthProperties swaggerUiOAuthProperties,
            SwaggerUiConfigParameters swaggerUiConfigParameters,
            SwaggerWelcomeCommon swaggerWelcomeCommon,
            ObjectMapperProvider objectMapperProvider) {
        super(swaggerUiConfig, swaggerUiOAuthProperties, swaggerUiConfigParameters, swaggerWelcomeCommon, objectMapperProvider);
    }

    @Override
    public Resource transform(HttpServletRequest requisicao, Resource recurso, ResourceTransformerChain cadeiaTransformacao)
            throws IOException {
        Resource recursoTransformado = super.transform(requisicao, recurso, cadeiaTransformacao);
        if (recurso.getFilename() == null || !recurso.getFilename().equalsIgnoreCase("index.html")) {
            return recursoTransformado;
        }

        String pagina;
        try (InputStream entrada = recursoTransformado.getInputStream()) {
            pagina = StreamUtils.copyToString(entrada, StandardCharsets.UTF_8);
        }
        pagina = pagina.replace("<title>Swagger UI</title>", "<title>BiblioTech | API</title>");
        pagina = pagina.replace("</head>", "<link rel=\"stylesheet\" href=\"/css/bibliotech-swagger.css?v=4\"></head>");

        Matcher tagCorpo = PADRAO_TAG_CORPO.matcher(pagina);
        if (tagCorpo.find()) {
            pagina = pagina.substring(0, tagCorpo.end()) + BANNER_BIBLIOTECH + pagina.substring(tagCorpo.end());
        }

        return new TransformedResource(recursoTransformado, pagina.getBytes(StandardCharsets.UTF_8));
    }
}



