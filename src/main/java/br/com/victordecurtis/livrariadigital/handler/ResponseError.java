package br.com.victordecurtis.livrariadigital.handler;

import lombok.Getter;
import lombok.Setter;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@Schema(description = "Formato padrão das respostas de erro da API.")
public class ResponseError {
    @Schema(description = "Data e hora em que o erro foi gerado.", example = "2026-11-12T11:16:20.422Z")
    private Date timestamp = new Date();
    @Schema(description = "Situação da resposta. Para erros, o valor é `error`.", example = "error")
    private String status = "error";
    @Schema(description = "Código HTTP da resposta.", example = "400")
    private int statusCode;
    @Schema(description = "Resumo do tipo de erro.", example = "Bad Request")
    private String error;
    @Schema(description = "Mensagens que explicam os campos inválidos ou a causa do erro.", example = "O título deve ter entre 2 e 160 caracteres")
    private List<String> errors;

    public ResponseError(String error, int statusCode, List<String> errors) {
        this.error = error;
        this.statusCode = statusCode;
        this.errors = errors;
    }
}
