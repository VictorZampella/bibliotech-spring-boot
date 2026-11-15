package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.Categoria;
import br.com.victordecurtis.livrariadigital.repository.CategoriaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoriaService {
    private final CategoriaRepository repositorioCategorias;

    public CategoriaService(CategoriaRepository repositorioCategorias) {
        this.repositorioCategorias = repositorioCategorias;
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
        repositorioCategorias.deleteById(id);
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

