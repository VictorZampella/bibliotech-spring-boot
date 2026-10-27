package br.com.victordecurtis.livrariadigital.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Table(name = "endereco")
@Entity
@Data
public class Endereco {
    @Id
    @Schema(description = "CEP com 8 dígitos, sem hífen. Os demais campos são preenchidos pela consulta ao ViaCEP.", example = "01001000")
    private String cep;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String logradouro;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String complemento;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String bairro;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String localidade;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String uf;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String ibge;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String gia;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String ddd;
    @Schema(description = "Preenchido automaticamente pelo ViaCEP.", accessMode = Schema.AccessMode.READ_ONLY)
    private String siafi;
}



