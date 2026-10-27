package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.Categoria;
import br.com.victordecurtis.livrariadigital.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CategoriaService {
    private final CategoriaRepository categoriaRepository;

    @Autowired
    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Page<Categoria> buscarTodos(Pageable pageable) {
        return categoriaRepository.findAll(pageable);
    }

    public Categoria buscarPorId(UUID id) {
        return categoriaRepository.findById(id).orElse(null);
    }

    public Categoria salvar(Categoria categoria) {
        if (categoria.getId() == null) {
            categoria.setId(UUID.randomUUID());
        }
        return categoriaRepository.save(categoria);
    }

    public void excluir(UUID id) {
        categoriaRepository.deleteById(id);
    }

    public UpsertResult<Categoria> atualizarOuCriar(UUID id, Categoria dados) {
        Categoria categoria = categoriaRepository.findById(id).orElse(null);
        boolean criado = categoria == null;

        if (criado) {
            categoria = dados;
            categoria.setId(id);
        } else {
            categoria.setNome(dados.getNome());
        }

        return new UpsertResult<>(categoriaRepository.save(categoria), criado);
    }
}

