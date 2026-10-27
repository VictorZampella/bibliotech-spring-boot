package br.com.victordecurtis.livrariadigital.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class Autor {
    @Id
    @Schema(description = "Identificador gerado pela API. No PUT, use o UUID informado na rota.", accessMode = Schema.AccessMode.READ_ONLY, example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64")
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "O nome do autor é obrigatório")
    @Schema(description = "Nome completo do autor.", example = "Machado de Assis")
    private String nome;

    @Column(nullable = false)
    @NotNull(message = "A data de nascimento do autor é obrigatória")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Data de nascimento no formato AAAA-MM-DD.", example = "1839-06-21")
    private Date dataNascimento;

    @Column(nullable = false, length = 1500)
    @NotBlank(message = "A biografia do autor é obrigatória")
    @Schema(description = "Biografia do autor.", example = "Escritor brasileiro, considerado um dos maiores nomes da literatura nacional.")
    private String biografia;

    @Column(nullable = false, length = 1500)
    @NotBlank(message = "A URL da foto do autor é obrigatória")
    @Schema(description = "URL pública da foto do autor.", example = "https://exemplo.com/imagens/machado-de-assis.jpg")
    private String urlFoto;

    @Column(nullable = false)
    @NotBlank(message = "A nacionalidade do autor é obrigatória")
    @Schema(description = "Nacionalidade do autor.", example = "Brasileira")
    private String nacionalidade;
}



