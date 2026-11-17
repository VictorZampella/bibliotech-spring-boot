package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.handler.BusinessException;
import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.model.Categoria;
import br.com.victordecurtis.livrariadigital.model.Livro;
import br.com.victordecurtis.livrariadigital.repository.AutorRepository;
import br.com.victordecurtis.livrariadigital.repository.CategoriaRepository;
import br.com.victordecurtis.livrariadigital.repository.LivroRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(CategoriaService.class)
class CategoriaServiceTest {
    private static final String MENSAGEM_CATEGORIA_VINCULADA = "A categoria está vinculada a um ou mais livros e não pode ser removida. "
            + "Atualize ou remova os livros vinculados antes de tentar novamente.";

    @Autowired
    private CategoriaService servicoCategorias;

    @Autowired
    private CategoriaRepository repositorioCategorias;

    @Autowired
    private AutorRepository repositorioAutores;

    @Autowired
    private LivroRepository repositorioLivros;

    @Test
    void categoriaVinculadaNaoPodeSerExcluida() {
        Categoria categoria = repositorioCategorias.save(criarCategoria());
        Autor autor = repositorioAutores.save(criarAutor());
        repositorioLivros.save(criarLivro(autor, categoria));

        BusinessException erro = assertThrows(BusinessException.class,
                () -> servicoCategorias.excluir(categoria.getId()));

        assertEquals(MENSAGEM_CATEGORIA_VINCULADA, erro.getMessage());
        assertTrue(repositorioCategorias.existsById(categoria.getId()));
    }

    @Test
    void categoriaSemLivrosPodeSerExcluida() {
        Categoria categoria = repositorioCategorias.save(criarCategoria());

        servicoCategorias.excluir(categoria.getId());

        assertFalse(repositorioCategorias.existsById(categoria.getId()));
    }

    private Categoria criarCategoria() {
        Categoria categoria = new Categoria();
        categoria.setId(UUID.randomUUID());
        categoria.setNome("Categoria teste " + UUID.randomUUID());
        return categoria;
    }

    private Autor criarAutor() {
        Autor autor = new Autor();
        autor.setId(UUID.randomUUID());
        autor.setNome("Autor de teste");
        autor.setDataNascimento(new Date(0));
        autor.setBiografia("Biografia do autor usado no teste de exclusão de categoria.");
        autor.setUrlFoto("https://exemplo.com/autor.jpg");
        autor.setNacionalidade("Brasileira");
        return autor;
    }

    private Livro criarLivro(Autor autor, Categoria categoria) {
        Livro livro = new Livro();
        livro.setId(UUID.randomUUID());
        livro.setTitulo("Livro de teste");
        livro.setAutor(autor);
        livro.setDescricaoCurta("Descrição curta de um livro utilizado no teste.");
        livro.setDescricaoLonga("Descrição longa de um livro utilizado para confirmar o vínculo entre livro e categoria.");
        livro.setUrlCapa("https://exemplo.com/capa.jpg");
        livro.setPreco(new BigDecimal("10.00"));
        livro.setCategoria(categoria);
        livro.setQuantidadePaginas(100);
        livro.setQuantidadeExemplares(1);
        livro.setAnoPublicacao(new Date(0));
        return livro;
    }
}
