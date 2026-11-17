package br.com.victordecurtis.livrariadigital.config;

import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.model.PapelAcesso;
import br.com.victordecurtis.livrariadigital.model.PlanoUtilizacao;
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
        String caminho = request.getServletPath();
        String metodo = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(metodo) || !ehRequisicaoDaApi(caminho)
                || ehAutenticacao(metodo, caminho)) {
            filterChain.doFilter(request, response);
            return;
        }

        String chave = request.getHeader("X-API-Key");
        ApiKeyService.Acesso acesso = acessoDaRequisicao(request, chave);
        if (chave != null && !chave.isBlank() && acesso == null) {
            responderErro(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                    "A chave X-API-Key é inválida ou foi revogada.");
            return;
        }
        if (acesso != null) request.setAttribute(ApiKeyService.ATRIBUTO_ACESSO, acesso);

        if ((acesso == null && ehConsultaPublicaDeLivro(metodo, caminho))
                || (acesso == null && ehCadastroPublicoDeLeitor(metodo, caminho))) {
            filterChain.doFilter(request, response);
            return;
        }

        if (acesso == null) {
            responderErro(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized",
                    "Faça login em POST /api/auth/login e envie a chave no cabeçalho X-API-Key.");
            return;
        }

        if (acesso.papel() == PapelAcesso.ADMIN || acesso.plano() == PlanoUtilizacao.PREMIUM) {
            filterChain.doFilter(request, response);
            return;
        }

        if (ehConsultaPermitidaAoMembro(metodo, caminho)) {
            filterChain.doFilter(request, response);
            return;
        }

        responderErro(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden",
                "O plano BASE permite consultar os recursos, mas somente PREMIUM ou ADMIN pode cadastrar, atualizar ou remover.");
    }

    private ApiKeyService.Acesso acessoDaRequisicao(HttpServletRequest request, String chave) {
        Object atributo = request.getAttribute(ApiKeyService.ATRIBUTO_ACESSO);
        if (atributo instanceof ApiKeyService.Acesso acesso) return acesso;
        return servicoChaves.obterAcesso(chave).orElse(null);
    }

    private boolean ehAutenticacao(String metodo, String caminho) {
        return "POST".equalsIgnoreCase(metodo)
                && ("/api/auth/login".equals(caminho) || "/api/auth/logout".equals(caminho));
    }

    private boolean ehConsultaPermitidaAoMembro(String metodo, String caminho) {
        if (!"GET".equalsIgnoreCase(metodo)) return false;
        return ehRecurso(metodo, caminho, "/api/livros")
                || ehRecurso(metodo, caminho, "/api/autores")
                || ehRecurso(metodo, caminho, "/api/categorias")
                || ehRecurso(metodo, caminho, "/api/usuarios");
    }

    private boolean ehConsultaPublicaDeLivro(String metodo, String caminho) {
        return "GET".equalsIgnoreCase(metodo)
                && ("/api/livros".equals(caminho) || caminho.startsWith("/api/livros/"));
    }

    private boolean ehRecurso(String metodo, String caminho, String recurso) {
        return "GET".equalsIgnoreCase(metodo)
                && (recurso.equals(caminho) || caminho.startsWith(recurso + "/"));
    }

    private boolean ehCadastroPublicoDeLeitor(String metodo, String caminho) {
        return "POST".equalsIgnoreCase(metodo) && "/api/usuarios".equals(caminho);
    }

    private void responderErro(HttpServletResponse response, int status, String erro, String mensagem)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ResponseError(erro, status, List.of(mensagem)));
    }

    private boolean ehRequisicaoDaApi(String caminho) {
        return caminho != null && (caminho.equals("/api") || caminho.startsWith("/api/"));
    }
}
