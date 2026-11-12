package br.com.victordecurtis.livrariadigital.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class Livro {
    @Id
    @Schema(description = "Identificador gerado pela API. No PUT, use o UUID informado na rota.", accessMode = Schema.AccessMode.READ_ONLY, example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64")
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "O título do livro é obrigatório")
    @Size(min = 2, max = 160, message = "O título deve ter entre 2 e 160 caracteres")
    @Schema(description = "Título da obra.", example = "Duna")
    private String titulo;

    @ManyToOne
    @JoinColumn(nullable = false, name = "autor_id")
    @NotNull(message = "O autor do livro é obrigatório")
    @Schema(description = "Autor já cadastrado. Informe o objeto com o UUID existente, por exemplo {\"id\":\"UUID_DO_AUTOR\"}.")
    private Autor autor;

    @Column(nullable = false)
    @NotBlank(message = "A descrição curta do livro é obrigatória")
    @Size(min = 20, max = 300, message = "A descrição curta deve ter entre 20 e 300 caracteres")
    @Schema(description = "Resumo curto exibido na listagem do catálogo.", example = "Uma jornada épica pelo planeta Arrakis.")
    private String descricaoCurta;

    @Column(nullable = false, length = 10000)
    @NotBlank(message = "A descrição longa do livro é obrigatória")
    @Size(min = 50, max = 10000, message = "A descrição longa deve ter entre 50 e 10000 caracteres")
    @Schema(description = "Descrição completa da obra.", example = "Em um futuro distante, famílias disputam o controle de Arrakis e de sua valiosa especiaria.")
    private String descricaoLonga;

    @Column(nullable = false, length = 1500)
    @NotBlank(message = "A URL da capa do livro é obrigatória")
    @Size(max = 1500, message = "A URL da capa deve ter no máximo 1500 caracteres")
    @Pattern(regexp = "(?i)^https?://\\S+$", message = "A capa deve ser informada por uma URL válida iniciada com http:// ou https://")
    @Schema(description = "URL pública da imagem da capa.", example = "https://exemplo.com/capas/duna.jpg")
    private String urlCapa;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "O preço do livro é obrigatório")
    @DecimalMin(value = "0.00", message = "O preço do livro deve ser maior ou igual a R$ 0,00")
    @Digits(integer = 8, fraction = 2, message = "O preço pode ter até 8 dígitos inteiros e 2 casas decimais")
    @Schema(description = "Preço da obra, em reais. Aceita zero ou valor positivo.", example = "49.90", minimum = "0")
    private BigDecimal preco;

    @ManyToOne
    @NotNull(message = "A categoria do livro é obrigatória")
    @Schema(description = "Categoria já cadastrada. Informe o objeto com o UUID existente, por exemplo {\"id\":\"UUID_DA_CATEGORIA\"}.")
    private Categoria categoria;

    @Column(nullable = false)
    @NotNull(message = "A quantidade de páginas do livro é obrigatória")
    @Min(value = 1, message = "A quantidade de páginas do livro deve ser maior que 0")
    @Max(value = 10000, message = "A quantidade de páginas não pode ultrapassar 10000")
    @Schema(description = "Quantidade total de páginas (mínimo 1).", example = "688", minimum = "1")
    private Integer quantidadePaginas;

    @Column(nullable = false)
    @NotNull(message = "O ano de publicação do livro é obrigatório")
    @PastOrPresent(message = "A data de publicação não pode ser futura")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Data de publicação no formato AAAA-MM-DD.", example = "1965-01-01")
    private Date anoPublicacao;
}



