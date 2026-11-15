package br.com.victordecurtis.livrariadigital.dto;

import br.com.victordecurtis.livrariadigital.model.Livro;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

@Schema(description = "Representação resumida do livro disponível na versão 2 da API.")
public record LivroVersao2Response(
        @Schema(description = "UUID do livro.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64")
        UUID id,
        @Schema(description = "Título da obra.", example = "Duna")
        String titulo,
        @Schema(description = "Nome do autor cadastrado.", example = "Frank Herbert")
        String autorNome,
        @Schema(description = "Categoria do livro.", example = "Ficção científica")
        String categoriaNome,
        @Schema(description = "Resumo da obra.")
        String descricaoCurta,
        @Schema(description = "Descrição completa da obra.")
        String descricaoLonga,
        @Schema(description = "Endereço da imagem da capa.", example = "https://example.com/duna.jpg")
        String urlCapa,
        @Schema(description = "Preço em reais.", example = "49.90")
        BigDecimal preco,
        @Schema(description = "Quantidade de páginas.", example = "688")
        Integer quantidadePaginas,
        @Schema(description = "Data de publicação no formato AAAA-MM-DD.", example = "1965-08-01")
        LocalDate anoPublicacao) {

    public static LivroVersao2Response de(Livro livro) {
        return new LivroVersao2Response(
                livro.getId(),
                livro.getTitulo(),
                livro.getAutor().getNome(),
                livro.getCategoria().getNome(),
                livro.getDescricaoCurta(),
                livro.getDescricaoLonga(),
                livro.getUrlCapa(),
                livro.getPreco(),
                livro.getQuantidadePaginas(),
                livro.getAnoPublicacao().toInstant().atZone(ZoneOffset.UTC).toLocalDate());
    }
}
