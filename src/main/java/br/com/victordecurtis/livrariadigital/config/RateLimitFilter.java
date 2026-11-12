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

/** Limita a quantidade de chamadas à API por IP para reduzir abuso e sobrecarga. */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {
    private static final long REQUESTS_PER_MINUTE = 20;
    private static final Duration REFILL_PERIOD = Duration.ofMinutes(1);
    private static final long BUCKET_IDLE_NANOS = Duration.ofMinutes(2).toNanos();

    private final Map<String, BucketEntry> bucketMap = new ConcurrentHashMap<>();
    private final AtomicLong requestsSinceCleanup = new AtomicLong();
    private final ObjectMapper objectMapper;

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private Bucket newBucket() {
        return Bucket.builder()
                .addLimit(limit -> limit
                        .capacity(REQUESTS_PER_MINUTE)
                        .refillIntervally(REQUESTS_PER_MINUTE, REFILL_PERIOD))
                .build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || !isApiRequest(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String ip = clientIp(request);
        long now = System.nanoTime();
        BucketEntry entry = bucketMap.computeIfAbsent(ip, ignored -> new BucketEntry(newBucket(), now));
        entry.lastUsedNanos = now;
        cleanupIdleBucketsPeriodically(now);

        ConsumptionProbe probe = entry.bucket.tryConsumeAndReturnRemaining(1);
        response.setHeader("X-Rate-Limit-Limit", String.valueOf(REQUESTS_PER_MINUTE));
        if (probe.isConsumed()) {
            response.setHeader("X-Rate-Limit-Remaining", String.valueOf(probe.getRemainingTokens()));
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfter = Math.max(1, TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill()) + 1);
        response.setHeader("Retry-After", String.valueOf(retryAfter));
        response.setStatus(429);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        ResponseError error = new ResponseError("Too Many Requests", 429,
                List.of("Limite de 20 requisições por minuto excedido. Aguarde o tempo indicado no cabeçalho Retry-After e tente novamente."));
        objectMapper.writeValue(response.getWriter(), error);
    }

    private boolean isApiRequest(String path) {
        return path != null && (path.equals("/api") || path.startsWith("/api/"));
    }

    /** No Render, o primeiro endereço de X-Forwarded-For representa o cliente original. */
    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String firstAddress = forwardedFor.split(",", 2)[0].trim();
            if (!firstAddress.isBlank() && firstAddress.length() <= 64) {
                return firstAddress;
            }
        }
        return request.getRemoteAddr();
    }

    /** Remove buckets inativos para que IPs de curta duração não acumulem para sempre. */
    private void cleanupIdleBucketsPeriodically(long now) {
        if (requestsSinceCleanup.incrementAndGet() % 256 != 0) {
            return;
        }
        bucketMap.entrySet().removeIf(entry -> now - entry.getValue().lastUsedNanos > BUCKET_IDLE_NANOS);
    }

    private static final class BucketEntry {
        private final Bucket bucket;
        private volatile long lastUsedNanos;

        private BucketEntry(Bucket bucket, long lastUsedNanos) {
            this.bucket = bucket;
            this.lastUsedNanos = lastUsedNanos;
        }
    }
}
