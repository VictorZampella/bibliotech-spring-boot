package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Autenticação", description = "Emita uma chave com seu cadastro de leitor; use-a no cabeçalho X-API-Key para executar operações DELETE e revogue-a ao sair.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite de 20 requisições por minuto excedido; aguarde o tempo informado em Retry-After.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class)))})
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final ApiKeyService apiKeyService;

    public AuthController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Operation(summary = "Entrar e gerar chave de API", description = "Valida o e-mail e a senha do leitor e emite uma chave X-API-Key. A chave é mostrada somente nesta resposta: copie-a e use-a no botão Authorize do Swagger ou no cabeçalho das requisições DELETE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada; chave de API emitida."),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos.", content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        ApiKeyService.IssuedApiKey issued = apiKeyService.login(request.email(), request.senha());
        LoginResponse body = new LoginResponse(issued.key(), issued.userId(), issued.name(),
                "Guarde esta chave. Ela não será exibida novamente; use-a no cabeçalho X-API-Key.");
        return ResponseEntity.ok().header("X-API-Key", issued.key()).body(body);
    }

    @Operation(summary = "Sair e revogar chave", description = "Revoga a chave enviada em X-API-Key. Depois do logout, essa chave deixa de autorizar operações DELETE. Se a chave já estiver revogada ou tiver sido removida com o cadastro do leitor, a operação continua respondendo 204.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave revogada."),
            @ApiResponse(responseCode = "401", description = "Cabeçalho X-API-Key ausente.", content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Chave emitida pelo endpoint de login.", example = "biblio_chave-emitida-no-login")
            @RequestHeader("X-API-Key") String apiKey) {
        apiKeyService.logout(apiKey);
        return ResponseEntity.noContent().build();
    }

    public record LoginRequest(
            @NotBlank @Email @Size(max = 254)
            @Schema(description = "E-mail cadastrado do leitor.", example = "leitor@example.com") String email,
            @NotBlank @Size(min = 8, max = 100)
            @Schema(description = "Senha cadastrada para o leitor.", example = "Leitor123", minLength = 8, maxLength = 100) String senha) {
    }

    public record LoginResponse(
            @Schema(description = "Chave secreta a ser enviada no cabeçalho X-API-Key; exibida somente no login.") String apiKey,
            UUID usuarioId,
            String nome,
            String mensagem) {
    }
}
