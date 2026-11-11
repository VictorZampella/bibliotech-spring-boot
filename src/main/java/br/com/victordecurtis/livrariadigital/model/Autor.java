package br.com.victordecurtis.livrariadigital.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(min = 2, max = 100, message = "O nome do autor deve ter entre 2 e 100 caracteres")
    @NotBlank(message = "O nome do autor é obrigatório")
    @Schema(description = "Nome completo do autor.", example = "Machado de Assis")
    private String nome;

    @Column(nullable = false)
    @NotNull(message = "A data de nascimento do autor é obrigatória")
    @Past(message = "A data de nascimento do autor deve ser anterior à data atual")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "Data de nascimento no formato AAAA-MM-DD.", example = "1839-06-21")
    private Date dataNascimento;

    @Column(nullable = false, length = 1500)
    @NotBlank(message = "A biografia do autor é obrigatória")
    @Size(min = 30, max = 1500, message = "A biografia deve ter entre 30 e 1500 caracteres")
    @Schema(description = "Biografia do autor.", example = "Escritor brasileiro, considerado um dos maiores nomes da literatura nacional.")
    private String biografia;

    @Column(nullable = false, length = 1500)
    @NotBlank(message = "A URL da foto do autor é obrigatória")
    @Size(max = 1500, message = "A URL da foto deve ter no máximo 1500 caracteres")
    @Pattern(regexp = "(?i)^https?://\\S+$", message = "A foto deve ser informada por uma URL válida iniciada com http:// ou https://")
    @Schema(description = "URL pública da foto do autor.", example = "https://exemplo.com/imagens/machado-de-assis.jpg")
    private String urlFoto;

    @Column(nullable = false)
    @NotBlank(message = "A nacionalidade do autor é obrigatória")
    @Size(min = 2, max = 60, message = "A nacionalidade deve ter entre 2 e 60 caracteres")
    @Schema(description = "Nacionalidade do autor.", example = "Brasileira")
    private String nacionalidade;
}



