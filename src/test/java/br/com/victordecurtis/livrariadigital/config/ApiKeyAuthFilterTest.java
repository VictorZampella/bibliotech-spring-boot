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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyAuthFilterTest {

    @Test
    void membroPodeConsultarLivros() throws Exception {
        ApiKeyService chaves = chaves("membro", PapelAcesso.MEMBRO);
        AtomicInteger encaminhadas = new AtomicInteger();
        ApiKeyAuthFilter filtro = new ApiKeyAuthFilter(chaves, new ObjectMapper());

        for (String caminho : new String[]{"/api/livros", "/api/livros/00000000-0000-0000-0000-000000000001",
                "/api/livros/pesquisa"}) {
            MockHttpServletResponse resposta = executar(filtro, "GET", caminho, "membro", encaminhadas);
            assertEquals(200, resposta.getStatus());
        }
        assertEquals(3, encaminhadas.get());
    }

    @Test
    void membroNaoPodeCadastrarAtualizarOuRemoverLivros() throws Exception {
        ApiKeyService chaves = chaves("membro", PapelAcesso.MEMBRO);
        AtomicInteger encaminhadas = new AtomicInteger();
        ApiKeyAuthFilter filtro = new ApiKeyAuthFilter(chaves, new ObjectMapper());

        for (String metodo : new String[]{"POST", "PUT", "DELETE"}) {
            String caminho = "POST".equals(metodo) ? "/api/livros"
                    : "/api/livros/00000000-0000-0000-0000-000000000001";
            MockHttpServletResponse resposta = executar(filtro, metodo, caminho, "membro", encaminhadas);
            assertEquals(403, resposta.getStatus());
            assertTrue(resposta.getContentAsString().contains("Membro"));
        }
        assertEquals(0, encaminhadas.get());
    }

    @Test
    void membroNaoPodeAcessarAutores() throws Exception {
        AtomicInteger encaminhadas = new AtomicInteger();
        MockHttpServletResponse resposta = executar(new ApiKeyAuthFilter(chaves("membro", PapelAcesso.MEMBRO),
                new ObjectMapper()), "GET", "/api/autores", "membro", encaminhadas);

        assertEquals(403, resposta.getStatus());
        assertTrue(resposta.getContentAsString().contains("Membro"));
        assertEquals(0, encaminhadas.get());
    }

    @Test
    void administradorPodeExecutarOperacoesDeLivro() throws Exception {
        ApiKeyService chaves = chaves("admin", PapelAcesso.ADMIN);
        AtomicInteger encaminhadas = new AtomicInteger();
        ApiKeyAuthFilter filtro = new ApiKeyAuthFilter(chaves, new ObjectMapper());

        for (String metodo : new String[]{"GET", "POST", "PUT", "DELETE"}) {
            String caminho = "GET".equals(metodo) || "POST".equals(metodo) ? "/api/livros"
                    : "/api/livros/00000000-0000-0000-0000-000000000001";
            MockHttpServletResponse resposta = executar(filtro, metodo, caminho, "admin", encaminhadas);
            assertEquals(200, resposta.getStatus());
        }
        assertEquals(4, encaminhadas.get());
    }

    @Test
    void rotaAdministrativaSemChaveRetorna401() throws Exception {
        AtomicInteger encaminhadas = new AtomicInteger();
        MockHttpServletResponse resposta = executar(new ApiKeyAuthFilter(new FakeApiKeyService(), new ObjectMapper()),
                "DELETE", "/api/livros/00000000-0000-0000-0000-000000000001", null, encaminhadas);

        assertEquals(401, resposta.getStatus());
        assertEquals(0, encaminhadas.get());
    }

    @Test
    void cadastroPublicoDeLeitorNaoExigeChave() throws Exception {
        AtomicInteger encaminhadas = new AtomicInteger();
        MockHttpServletResponse resposta = executar(new ApiKeyAuthFilter(new FakeApiKeyService(), new ObjectMapper()),
                "POST", "/api/usuarios", null, encaminhadas);

        assertEquals(200, resposta.getStatus());
        assertEquals(1, encaminhadas.get());
    }

    private FakeApiKeyService chaves(String chave, PapelAcesso papel) {
        FakeApiKeyService servico = new FakeApiKeyService();
        servico.registrar(chave, new ApiKeyService.Acesso(
                "hash-" + chave, UUID.randomUUID(), papel, PlanoUtilizacao.BASE));
        return servico;
    }

    private MockHttpServletResponse executar(ApiKeyAuthFilter filtro, String metodo, String caminho,
                                             String chave, AtomicInteger encaminhadas) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest(metodo, caminho);
        requisicao.setServletPath(caminho);
        if (chave != null) requisicao.addHeader("X-API-Key", chave);
        MockHttpServletResponse resposta = new MockHttpServletResponse();
        FilterChain cadeia = (pedido, retorno) -> encaminhadas.incrementAndGet();
        filtro.doFilterInternal(requisicao, resposta, cadeia);
        return resposta;
    }
}
