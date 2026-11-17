package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.Categoria;
import br.com.victordecurtis.livrariadigital.handler.BusinessException;
import br.com.victordecurtis.livrariadigital.repository.CategoriaRepository;
import br.com.victordecurtis.livrariadigital.repository.LivroRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoriaService {
    private static final String ERRO_CATEGORIA_VINCULADA = "A categoria está vinculada a um ou mais livros e não pode ser removida. Atualize ou remova os livros vinculados antes de tentar novamente.";

    private final CategoriaRepository repositorioCategorias;
    private final LivroRepository repositorioLivros;

    public CategoriaService(CategoriaRepository repositorioCategorias, LivroRepository repositorioLivros) {
        this.repositorioCategorias = repositorioCategorias;
        this.repositorioLivros = repositorioLivros;
    }

    public Page<Categoria> buscarTodos(Pageable paginacao) {
        return repositorioCategorias.findAll(paginacao);
    }

    public Categoria buscarPorId(UUID id) {
        return repositorioCategorias.findById(id).orElse(null);
    }

    public Categoria salvar(Categoria categoria) {
        if (categoria.getId() == null) {
            categoria.setId(UUID.randomUUID());
        }
        return repositorioCategorias.save(categoria);
    }

    public void excluir(UUID id) {
        if (repositorioLivros.existsByCategoria_Id(id)) {
            throw new BusinessException(ERRO_CATEGORIA_VINCULADA);
        }

        try {
            repositorioCategorias.deleteById(id);
        } catch (DataIntegrityViolationException excecao) {
            throw new BusinessException(ERRO_CATEGORIA_VINCULADA);
        }
    }

    public UpsertResult<Categoria> atualizarOuCriar(UUID id, Categoria dados) {
        Categoria categoria = repositorioCategorias.findById(id).orElse(null);
        boolean criado = categoria == null;

        if (criado) {
            categoria = dados;
            categoria.setId(id);
        } else {
            categoria.setNome(dados.getNome());
        }

        return new UpsertResult<>(repositorioCategorias.save(categoria), criado);
    }
}

