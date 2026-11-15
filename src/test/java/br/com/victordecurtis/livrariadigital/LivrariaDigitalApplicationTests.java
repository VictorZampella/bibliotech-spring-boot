package br.com.victordecurtis.livrariadigital;

import br.com.victordecurtis.livrariadigital.model.Livro;
import br.com.victordecurtis.livrariadigital.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LivrariaDigitalApplicationTests {

    @Autowired
    private MockMvc requisicoes;

    @Autowired
    private LivroRepository repositorioLivros;

    @Test
    void contextLoads() {
    }

    @Test
    void consultaLivroNaVersaoUmIncluiLinksDeNavegacao() throws Exception {
        Livro livro = primeiroLivroCadastrado();

        requisicoes.perform(get("/api/livros/{id}", livro.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.update.href").exists())
                .andExpect(jsonPath("$._links.delete.href").exists())
                .andExpect(jsonPath("$._links.autor.href").exists())
                .andExpect(jsonPath("$._links.categoria.href").exists())
                .andExpect(jsonPath("$.autor.id").exists())
                .andExpect(jsonPath("$.categoria.id").exists());
    }

    @Test
    void consultaLivroNaVersaoDoisRetornaNomesDeAutorECategoria() throws Exception {
        Livro livro = primeiroLivroCadastrado();

        requisicoes.perform(get("/api/livros/{id}", livro.getId())
                        .header("X-API-Version", "2")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.autorNome").isNotEmpty())
                .andExpect(jsonPath("$.categoriaNome").isNotEmpty())
                .andExpect(jsonPath("$._links.self.href").exists());
    }

    @Test
    void rejeitaVersaoDeApiNaoSuportada() throws Exception {
        Livro livro = primeiroLivroCadastrado();

        requisicoes.perform(get("/api/livros/{id}", livro.getId())
                        .header("X-API-Version", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.statusCode").value(400))
                .andExpect(jsonPath("$.errors[0]").value(
                        "Versão não suportada. Use X-API-Version: 1 ou X-API-Version: 2."));
    }

    @Test
    void listagemRetornaLinksDePaginacao() throws Exception {
        requisicoes.perform(get("/api/livros").param("page", "0").param("size", "1")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$._links.self.href").exists())
                .andExpect(jsonPath("$._links.next.href").exists())
                .andExpect(jsonPath("$.page.totalElements").isNumber());
    }

    private Livro primeiroLivroCadastrado() {
        return repositorioLivros.findAll().stream().findFirst()
                .orElseThrow(() -> new AssertionError("Os livros de demonstração não foram carregados."));
    }
}
