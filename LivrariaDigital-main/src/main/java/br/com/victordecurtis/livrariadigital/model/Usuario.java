package br.com.victordecurtis.livrariadigital.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Data
public class Usuario {
    @Id
    @Schema(description = "Identificador gerado pela API. No PUT, use o UUID informado na rota.", accessMode = Schema.AccessMode.READ_ONLY, example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64")
    private UUID id;

    @Column(nullable = false)
    @NotBlank(message = "O nome do usuário é obrigatório")
    @Schema(description = "Nome completo do leitor.", example = "Victor de Curtis")
    private String nome;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "O email do usuário é obrigatório")
    @Pattern(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,4}", message = "O email deve estar no formato adequado")
    @Schema(description = "E-mail válido e exclusivo para o cadastro.", example = "leitor@example.com")
    private String email;

    @Column(nullable = false, unique = true)
    @NotBlank(message = "O telefone do usuário é obrigatório")
    @Pattern(regexp = "\\([0-9]{2}\\) [0-9]{4,5}-[0-9]{4}", message = "O telefone deve estar no formato (99) 99999-9999")
    @Schema(description = "Telefone exclusivo no formato (11) 99999-9999.", example = "(11) 99999-1234")
    private String telefone;

    @Column(nullable = false)
    @NotBlank(message = "A senha do usuário é obrigatória")
    @Size(min = 8, message = "A senha deve conter pelo menos 8 caracteres (excluindo espaços em branco)")
    @Pattern(regexp = "^\\S{8,}$", message = "A senha não pode conter espaços em branco")
    @Schema(description = "Senha com pelo menos 8 caracteres, sem espaços.", example = "Leitor123")
    private String senha;

    @Column(nullable = false)
    @NotNull(message = "A data de nascimento do usuário é obrigatória")
    @Schema(description = "Data de nascimento em formato ISO 8601.", example = "1998-05-23T00:00:00.000Z")
    private Date dataNascimento;

    @ManyToOne
    @JoinColumn(nullable = false)
    @NotNull(message = "O endereço do usuário é obrigatório")
    @Schema(description = "Informe o CEP; a API consulta o ViaCEP e completa os demais dados do endereço. Exemplo: {\"cep\":\"01001000\"}.")
    private Endereco endereco;
}



