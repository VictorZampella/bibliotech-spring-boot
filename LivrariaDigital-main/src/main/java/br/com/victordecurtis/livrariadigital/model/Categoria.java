package br.com.victordecurtis.livrariadigital.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Entity
@Data
public class Categoria {
    @Id
    @Schema(description = "Identificador gerado pela API. No PUT, use o UUID informado na rota.", accessMode = Schema.AccessMode.READ_ONLY, example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64")
    private UUID id;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "O nome da categoria é obrigatório")
    @Schema(description = "Nome único do gênero ou categoria. Não pode repetir outro cadastro.", example = "Ficção científica")
    private String nome;
}



