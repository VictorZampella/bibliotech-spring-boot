package br.com.victordecurtis.livrariadigital.config;

import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@Order(2)
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    private final ApiKeyService servicoChaves;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthFilter(ApiKeyService servicoChaves, ObjectMapper objectMapper) {
        this.servicoChaves = servicoChaves;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!"DELETE".equalsIgnoreCase(request.getMethod()) || !ehRequisicaoDaApi(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        String chave = request.getHeader("X-API-Key");
        if (servicoChaves.chaveValida(chave)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ResponseError(
                "Unauthorized", 401, List.of("Envie uma chave válida no cabeçalho X-API-Key. Faça login em POST /api/auth/login.")));
    }

    private boolean ehRequisicaoDaApi(String caminho) {
        return caminho != null && (caminho.equals("/api") || caminho.startsWith("/api/"));
    }
}
