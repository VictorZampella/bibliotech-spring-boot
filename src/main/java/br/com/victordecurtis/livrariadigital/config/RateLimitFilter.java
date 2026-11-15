package br.com.victordecurtis.livrariadigital.config;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final long REQUISICOES_POR_MINUTO = 20;
    private static final Duration PERIODO_REPOSICAO = Duration.ofMinutes(1);
    private static final long TEMPO_INATIVO_NANOS = Duration.ofMinutes(2).toNanos();

    private final Map<String, EntradaBalde> baldesPorIp = new ConcurrentHashMap<>();
    private final AtomicLong requisicoesDesdeLimpeza = new AtomicLong();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private Bucket novoBalde() {
        return Bucket.builder()
                .addLimit(limite -> limite
                        .capacity(REQUISICOES_POR_MINUTO)
                        .refillIntervally(REQUISICOES_POR_MINUTO, PERIODO_REPOSICAO))
                .build();
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

        String ip = obterIpCliente(request);
        long agora = System.nanoTime();
        EntradaBalde entrada = baldesPorIp.computeIfAbsent(ip, ignorado -> new EntradaBalde(novoBalde(), agora));
        entrada.ultimoUsoNanos = agora;
        limparBaldesInativosPeriodicamente(agora);

        ConsumptionProbe consumo = entrada.balde.tryConsumeAndReturnRemaining(1);
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(REQUISICOES_POR_MINUTO));
        if (consumo.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(consumo.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long segundosAguardar = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(consumo.getNanosToWaitForRefill()) + 1);
        response.setHeader("Retry-After", String.valueOf(segundosAguardar));
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ResponseError erro = new ResponseError("Too Many Requests", 429,
                List.of("Limite de 20 requisições por minuto excedido. Aguarde o tempo indicado no cabeçalho Retry-After e tente novamente."));
        objectMapper.writeValue(response.getWriter(), erro);
    }

    private boolean ehRequisicaoDaApi(String caminho) {
        return caminho != null && (caminho.equals("/api") || caminho.startsWith("/api/"));
    }

    /** No Render, o primeiro endereço de X-Forwarded-For representa o cliente original. */
    private String obterIpCliente(HttpServletRequest request) {
        String enderecosEncaminhados = request.getHeader("X-Forwarded-For");
        if (enderecosEncaminhados != null && !enderecosEncaminhados.isBlank()) {
            String primeiroEndereco = enderecosEncaminhados.split(",", 2)[0].trim();
            if (!primeiroEndereco.isBlank() && primeiroEndereco.length() <= 64) {
                return primeiroEndereco;
            }
        }
        return request.getRemoteAddr();
    }

    private void limparBaldesInativosPeriodicamente(long agora) {
        if (requisicoesDesdeLimpeza.incrementAndGet() % 256 != 0) {
            return;
        }
        baldesPorIp.entrySet().removeIf(entrada -> agora - entrada.getValue().ultimoUsoNanos > TEMPO_INATIVO_NANOS);
    }

    private static final class EntradaBalde {
        private final Bucket balde;
        private volatile long ultimoUsoNanos;

        private EntradaBalde(Bucket balde, long ultimoUsoNanos) {
            this.balde = balde;
            this.ultimoUsoNanos = ultimoUsoNanos;
        }
    }
}
