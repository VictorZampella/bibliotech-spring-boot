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
    private static final Pattern BODY_TAG = Pattern.compile("(?i)<body[^>]*>");
    private static final String BRAND_BANNER = """
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
    public Resource transform(HttpServletRequest request, Resource resource, ResourceTransformerChain transformer)
            throws IOException {
        Resource transformed = super.transform(request, resource, transformer);
        if (resource.getFilename() == null || !resource.getFilename().equalsIgnoreCase("index.html")) {
            return transformed;
        }

        String html;
        try (InputStream input = transformed.getInputStream()) {
            html = StreamUtils.copyToString(input, StandardCharsets.UTF_8);
        }
        html = html.replace("<title>Swagger UI</title>", "<title>BiblioTech | API</title>");
        html = html.replace("</head>", "<link rel=\"stylesheet\" href=\"/css/bibliotech-swagger.css?v=4\"></head>");

        Matcher bodyTag = BODY_TAG.matcher(html);
        if (bodyTag.find()) {
            html = html.substring(0, bodyTag.end()) + BRAND_BANNER + html.substring(bodyTag.end());
        }

        return new TransformedResource(transformed, html.getBytes(StandardCharsets.UTF_8));
    }
}



