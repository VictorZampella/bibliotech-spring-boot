package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.repository.AutorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AutorService {
    private final AutorRepository repositorioAutores;

    public AutorService(AutorRepository repositorioAutores) {
        this.repositorioAutores = repositorioAutores;
    }

    public Page<Autor> buscarTodos(Pageable paginacao) {
        return repositorioAutores.findAll(paginacao);
    }

    public Autor buscarPorId(UUID id) {
        return repositorioAutores.findById(id).orElse(null);
    }

    public Autor salvar(Autor autor) {
        if (autor.getId() == null) {
            autor.setId(UUID.randomUUID());
        }
        return repositorioAutores.save(autor);
    }

    public void excluir(UUID id) {
        repositorioAutores.deleteById(id);
    }

    public UpsertResult<Autor> atualizarOuCriar(UUID id, Autor dados) {
        Autor autor = repositorioAutores.findById(id).orElse(null);
        boolean criado = autor == null;

        if (criado) {
            autor = dados;
            autor.setId(id);
        } else {
            autor.setNome(dados.getNome());
            autor.setDataNascimento(dados.getDataNascimento());
            autor.setBiografia(dados.getBiografia());
            autor.setUrlFoto(dados.getUrlFoto());
            autor.setNacionalidade(dados.getNacionalidade());
        }

        return new UpsertResult<>(repositorioAutores.save(autor), criado);
    }
}

