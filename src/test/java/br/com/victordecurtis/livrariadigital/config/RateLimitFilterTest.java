package br.com.victordecurtis.livrariadigital.config;

import br.com.victordecurtis.livrariadigital.model.PapelAcesso;
import br.com.victordecurtis.livrariadigital.model.PlanoUtilizacao;
import br.com.victordecurtis.livrariadigital.service.ApiKeyService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitFilterTest {

    @Test
    void limitaChaveBaseA30RequisicoesPorMinuto() throws Exception {
        FakeApiKeyService chaves = new FakeApiKeyService();
        chaves.registrar("chave-base", new ApiKeyService.Acesso(
                "hash-base", UUID.randomUUID(), PapelAcesso.MEMBRO, PlanoUtilizacao.BASE));
        RateLimitFilter filtro = filtro(chaves);
        AtomicInteger encaminhadas = new AtomicInteger();

        for (int i = 0; i < 30; i++) {
            executar(filtro, "chave-base", "198.51.100.1", encaminhadas);
        }
        MockHttpServletResponse resposta = executar(filtro, "chave-base", "198.51.100.1", encaminhadas);

        assertEquals(429, resposta.getStatus());
        assertEquals("30", resposta.getHeader("X-Rate-Limit-Key-Limit"));
        assertRetryAfter(resposta);
        assertEquals(30, encaminhadas.get());
    }

    @Test
    void limitaChavePremiumA100RequisicoesPorMinuto() throws Exception {
        FakeApiKeyService chaves = new FakeApiKeyService();
        chaves.registrar("chave-premium", new ApiKeyService.Acesso(
                "hash-premium", UUID.randomUUID(), PapelAcesso.MEMBRO, PlanoUtilizacao.PREMIUM));
        RateLimitFilter filtro = filtro(chaves);
        AtomicInteger encaminhadas = new AtomicInteger();

        for (int i = 0; i < 100; i++) {
            executar(filtro, "chave-premium", "198.51.100.2", encaminhadas);
        }
        MockHttpServletResponse resposta = executar(filtro, "chave-premium", "198.51.100.2", encaminhadas);

        assertEquals(429, resposta.getStatus());
        assertEquals("100", resposta.getHeader("X-Rate-Limit-Key-Limit"));
        assertRetryAfter(resposta);
        assertEquals(100, encaminhadas.get());
    }

    @Test
    void limitaEnderecoIpA120RequisicoesPorMinuto() throws Exception {
        RateLimitFilter filtro = filtro(new FakeApiKeyService());
        AtomicInteger encaminhadas = new AtomicInteger();

        for (int i = 0; i < 120; i++) {
            executar(filtro, null, "198.51.100.3", encaminhadas);
        }
        MockHttpServletResponse resposta = executar(filtro, null, "198.51.100.3", encaminhadas);

        assertEquals(429, resposta.getStatus());
        assertEquals("120", resposta.getHeader("X-Rate-Limit-IP-Limit"));
        assertRetryAfter(resposta);
        assertEquals(120, encaminhadas.get());
    }

    @Test
    void contabilizaCadaChaveSeparadamenteDoLimiteDeIp() throws Exception {
        FakeApiKeyService chaves = new FakeApiKeyService();
        chaves.registrar("chave-um", new ApiKeyService.Acesso(
                "hash-um", UUID.randomUUID(), PapelAcesso.MEMBRO, PlanoUtilizacao.BASE));
        chaves.registrar("chave-dois", new ApiKeyService.Acesso(
                "hash-dois", UUID.randomUUID(), PapelAcesso.MEMBRO, PlanoUtilizacao.BASE));
        RateLimitFilter filtro = filtro(chaves);
        AtomicInteger encaminhadas = new AtomicInteger();

        for (int i = 0; i < 30; i++) {
            executar(filtro, "chave-um", "198.51.100.4", encaminhadas);
            executar(filtro, "chave-dois", "198.51.100.4", encaminhadas);
        }
        MockHttpServletResponse resposta = executar(filtro, "chave-um", "198.51.100.4", encaminhadas);

        assertEquals(429, resposta.getStatus());
        assertEquals("59", resposta.getHeader("X-Rate-Limit-IP-Remaining"));
        assertEquals("0", resposta.getHeader("X-Rate-Limit-Key-Remaining"));
        assertEquals(60, encaminhadas.get());
    }

    private void assertRetryAfter(MockHttpServletResponse resposta) {
        assertNotNull(resposta.getHeader("Retry-After"));
        assertTrue(Integer.parseInt(resposta.getHeader("Retry-After")) > 0);
    }

    private RateLimitFilter filtro(ApiKeyService chaves) {
        RateLimitProperties configuracao = new RateLimitProperties();
        return new RateLimitFilter(chaves, configuracao, new ObjectMapper());
    }

    private MockHttpServletResponse executar(RateLimitFilter filtro, String chave, String ip,
                                             AtomicInteger encaminhadas) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest("GET", "/api/livros");
        requisicao.setServletPath("/api/livros");
        requisicao.setRemoteAddr("192.0.2.10");
        requisicao.addHeader("CF-Connecting-IP", ip);
        if (chave != null) requisicao.addHeader("X-API-Key", chave);
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        FilterChain cadeia = (pedido, retorno) -> encaminhadas.incrementAndGet();
        filtro.doFilterInternal(requisicao, resposta, cadeia);
        return resposta;
    }
}
