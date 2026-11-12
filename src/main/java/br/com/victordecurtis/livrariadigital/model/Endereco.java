package br.com.victordecurtis.livrariadigital.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Table(name = "endereco")
@Entity
@Data
public class Endereco {
    @Id
    @Schema(description = "Campo que deve ser enviado no cadastro do leitor: CEP com exatamente 8 números, sem hífen ou espaços. A API consulta o ViaCEP para obter os demais dados.", example = "01001000", minLength = 8, maxLength = 8, pattern = "^[0-9]{8}$")
    @NotBlank(message = "O CEP do endereço é obrigatório")
    @Pattern(regexp = "^[0-9]{8}$", message = "O CEP deve conter exatamente 8 números, sem hífen ou espaços")
    private String cep;
    @Schema(description = "Nome da rua ou avenida, retornado pelo ViaCEP; pode não estar disponível para todos os CEPs.", accessMode = Schema.AccessMode.READ_ONLY, example = "Praça da Sé")
    private String logradouro;
    @Schema(description = "Complemento do endereço, quando informado no ViaCEP. Pode vir vazio.", accessMode = Schema.AccessMode.READ_ONLY, example = "lado ímpar")
    private String complemento;
    @Schema(description = "Bairro retornado pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY, example = "Sé")
    private String bairro;
    @Schema(description = "Cidade ou município retornado pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY, example = "São Paulo")
    private String localidade;
    @Schema(description = "Sigla da unidade federativa retornada pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY, example = "SP", minLength = 2, maxLength = 2)
    private String uf;
    @Schema(description = "Código do município no IBGE, retornado pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY, example = "3550308")
    private String ibge;
    @Schema(description = "Código GIA retornado pelo ViaCEP, quando disponível.", accessMode = Schema.AccessMode.READ_ONLY, example = "1004")
    private String gia;
    @Schema(description = "DDD da localidade retornado pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY, example = "11")
    private String ddd;
    @Schema(description = "Código SIAFI do município, retornado pelo ViaCEP quando disponível.", accessMode = Schema.AccessMode.READ_ONLY, example = "7107")
    private String siafi;
}



