package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.Livro;
import br.com.victordecurtis.livrariadigital.repository.LivroRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class LivroService {
    private final LivroRepository livroRepository;

    @Autowired
    public LivroService(LivroRepository livroRepository) {
        this.livroRepository = livroRepository;
    }

    public Page<Livro> buscarTodos(Pageable pageable) {
        return livroRepository.findAll(pageable);
    }

    public Livro buscarPorId(UUID id) {
        return livroRepository.findById(id).orElse(null);
    }

    public Page<Livro> buscarPorPalavraChave(String busca, Pageable pageable) {
        List<Livro> resultados = livroRepository.findAll().stream()
                .filter(livro -> livro.getTitulo().toLowerCase().contains(busca.toLowerCase()) ||
                        livro.getDescricaoCurta().toLowerCase().contains(busca.toLowerCase()) ||
                        livro.getDescricaoLonga().toLowerCase().contains(busca.toLowerCase()) ||
                        livro.getAutor().getNome().toLowerCase().contains(busca.toLowerCase()) ||
                        livro.getCategoria().getNome().toLowerCase().contains(busca.toLowerCase()) ||
                        livro.getAnoPublicacao().toString().contains(busca) ||
                        livro.getPreco().toString().contains(busca) ||
                        livro.getQuantidadePaginas().toString().contains(busca))
                .collect(Collectors.toList());

        Comparator<Livro> ordenacao = null;
        for (Sort.Order ordem : pageable.getSort()) {
            Comparator<Livro> proximaOrdenacao = comparadorPara(ordem);
            ordenacao = ordenacao == null ? proximaOrdenacao : ordenacao.thenComparing(proximaOrdenacao);
        }
        if (ordenacao != null) {
            resultados.sort(ordenacao);
        }

        int inicio = (int) Math.min(pageable.getOffset(), resultados.size());
        int fim = (int) Math.min((long) inicio + pageable.getPageSize(), resultados.size());
        return new PageImpl<>(resultados.subList(inicio, fim), pageable, resultados.size());
    }

    private Comparator<Livro> comparadorPara(Sort.Order ordem) {
        Comparator<Livro> comparador = switch (ordem.getProperty()) {
            case "preco" -> Comparator.comparing(Livro::getPreco);
            case "anoPublicacao" -> Comparator.comparing(Livro::getAnoPublicacao);
            case "quantidadePaginas" -> Comparator.comparing(Livro::getQuantidadePaginas);
            case "autor.nome" -> Comparator.comparing(livro -> livro.getAutor().getNome(), String.CASE_INSENSITIVE_ORDER);
            case "categoria.nome" -> Comparator.comparing(livro -> livro.getCategoria().getNome(), String.CASE_INSENSITIVE_ORDER);
            case "descricaoCurta" -> Comparator.comparing(Livro::getDescricaoCurta, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(Livro::getTitulo, String.CASE_INSENSITIVE_ORDER);
        };
        return ordem.isDescending() ? comparador.reversed() : comparador;
    }

    public Livro salvar(Livro livro) {
        if (livro.getId() == null) {
            livro.setId(UUID.randomUUID());
        }
        return livroRepository.save(livro);
    }

    public void excluir(UUID id) {
        livroRepository.deleteById(id);
    }

    public UpsertResult<Livro> atualizarOuCriar(UUID id, Livro dados) {
        Livro livro = livroRepository.findById(id).orElse(null);
        boolean criado = livro == null;

        if (criado) {
            livro = dados;
            livro.setId(id);
        } else {
            livro.setTitulo(dados.getTitulo());
            livro.setDescricaoCurta(dados.getDescricaoCurta());
            livro.setDescricaoLonga(dados.getDescricaoLonga());
            livro.setUrlCapa(dados.getUrlCapa());
            livro.setPreco(dados.getPreco());
            livro.setAnoPublicacao(dados.getAnoPublicacao());
            livro.setQuantidadePaginas(dados.getQuantidadePaginas());
            livro.setCategoria(dados.getCategoria());
            livro.setAutor(dados.getAutor());
        }

        return new UpsertResult<>(livroRepository.save(livro), criado);
    }
}

