package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.repository.AutorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AutorService {
    private final AutorRepository autorRepository;

    @Autowired
    public AutorService(AutorRepository autorRepository) {
        this.autorRepository = autorRepository;
    }

    public Page<Autor> buscarTodos(Pageable pageable) {
        return autorRepository.findAll(pageable);
    }

    public Autor buscarPorId(UUID id) {
        return autorRepository.findById(id).orElse(null);
    }

    public Autor salvar(Autor autor) {
        if (autor.getId() == null) {
            autor.setId(UUID.randomUUID());
        }
        return autorRepository.save(autor);
    }

    public void excluir(UUID id) {
        autorRepository.deleteById(id);
    }

    public UpsertResult<Autor> atualizarOuCriar(UUID id, Autor dados) {
        Autor autor = autorRepository.findById(id).orElse(null);
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

        return new UpsertResult<>(autorRepository.save(autor), criado);
    }
}

