package br.com.victordecurtis.livrariadigital.config;

import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.model.PlanoUtilizacao;
import br.com.victordecurtis.livrariadigital.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final String CABECALHO_IP_LIMITE = "X-Rate-Limit-IP-Limit";
    private static final String CABECALHO_IP_RESTANTE = "X-Rate-Limit-IP-Remaining";
    private static final String CABECALHO_CHAVE_LIMITE = "X-Rate-Limit-Key-Limit";
    private static final String CABECALHO_CHAVE_RESTANTE = "X-Rate-Limit-Key-Remaining";
    private static final long TEMPO_INATIVO_NANOS = Duration.ofMinutes(2).toNanos();

    private final Map<String, EntradaBalde> baldesPorIp = new ConcurrentHashMap<>();
    private final Map<String, EntradaBalde> baldesPorChave = new ConcurrentHashMap<>();
    private final AtomicLong requisicoesDesdeLimpeza = new AtomicLong();
    private final ApiKeyService servicoChaves;
    private final RateLimitProperties configuracao;
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ApiKeyService servicoChaves, RateLimitProperties configuracao, ObjectMapper objectMapper) {
        this.servicoChaves = servicoChaves;
        this.configuracao = configuracao;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String caminho = request.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !ehRequisicaoDaApi(caminho)) {
            filterChain.doFilter(request, response);
            return;
        }

        long agora = System.nanoTime();
        limparBaldesInativosPeriodicamente(agora);
        ConsumptionProbe consumoIp = consumir(baldesPorIp, obterIpCliente(request), configuracao.getIpLimit(), agora);
        response.setHeader(CABECALHO_IP_LIMITE, String.valueOf(configuracao.getIpLimit()));
        response.setHeader(CABECALHO_IP_RESTANTE, String.valueOf(consumoIp.getRemainingTokens()));
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(configuracao.getIpLimit()));
        response.setHeader("X-Rate-Limit-Remaining", String.valueOf(consumoIp.getRemainingTokens()));

        if (!consumoIp.isConsumed()) {
            responderLimite(response, consumoIp, "Limite de requisições por endereço IP excedido.");
            return;
        }

        var acesso = servicoChaves.obterAcesso(request.getHeader("X-API-Key"));
        if (acesso.isPresent()) {
            ApiKeyService.Acesso usuario = acesso.get();
            request.setAttribute(ApiKeyService.ATRIBUTO_ACESSO, usuario);
            long limite = usuario.plano() == PlanoUtilizacao.PREMIUM
                    ? configuracao.getPremiumLimit() : configuracao.getBaseLimit();
            ConsumptionProbe consumoChave = consumir(baldesPorChave, usuario.identificadorChave(), limite, agora);
            response.setHeader(CABECALHO_CHAVE_LIMITE, String.valueOf(limite));
            response.setHeader(CABECALHO_CHAVE_RESTANTE, String.valueOf(consumoChave.getRemainingTokens()));
            if (!consumoChave.isConsumed()) {
                responderLimite(response, consumoChave,
                        "Limite do plano " + usuario.plano() + " excedido para esta chave de API.");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private ConsumptionProbe consumir(Map<String, EntradaBalde> baldes, String identificador,
                                      long limite, long agora) {
        EntradaBalde entrada = baldes.compute(identificador, (ignorado, atual) ->
                atual == null || atual.limite != limite
                        ? new EntradaBalde(novoBalde(limite), limite, agora)
                        : atual);
        entrada.ultimoUsoNanos = agora;
        return entrada.balde.tryConsumeAndReturnRemaining(1);
    }

    private Bucket novoBalde(long limite) {
        return Bucket.builder()
                .addLimit(config -> config.capacity(limite)
                        .refillIntervally(limite, configuracao.getWindow()))
                .build();
    }

    private void responderLimite(HttpServletResponse response, ConsumptionProbe consumo, String mensagem)
            throws IOException {
        long segundos = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(consumo.getNanosToWaitForRefill()) + 1);
        response.setHeader("Retry-After", String.valueOf(segundos));
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), new ResponseError("Too Many Requests", 429, List.of(
                mensagem + " Aguarde " + segundos + " segundo(s), conforme Retry-After, antes de tentar novamente.")));
    }

    private boolean ehRequisicaoDaApi(String caminho) {
        return caminho != null && (caminho.equals("/api") || caminho.startsWith("/api/"));
    }

    private String obterIpCliente(HttpServletRequest request) {
        String ipCloudflare = request.getHeader("CF-Connecting-IP");
        if (ipCloudflare != null && !ipCloudflare.isBlank() && ipCloudflare.length() <= 64
                && ipCloudflare.matches("[0-9a-fA-F:.]+")) {
            return ipCloudflare;
        }
        return request.getRemoteAddr() == null ? "desconhecido" : request.getRemoteAddr();
    }

    private void limparBaldesInativosPeriodicamente(long agora) {
        if (requisicoesDesdeLimpeza.incrementAndGet() % 256 != 0) return;
        baldesPorIp.entrySet().removeIf(item -> agora - item.getValue().ultimoUsoNanos > TEMPO_INATIVO_NANOS);
        baldesPorChave.entrySet().removeIf(item -> agora - item.getValue().ultimoUsoNanos > TEMPO_INATIVO_NANOS);
    }

    private static final class EntradaBalde {
        private final Bucket balde;
        private final long limite;
        private volatile long ultimoUsoNanos;

        private EntradaBalde(Bucket balde, long limite, long ultimoUsoNanos) {
            this.balde = balde;
            this.limite = limite;
            this.ultimoUsoNanos = ultimoUsoNanos;
        }
    }
}
