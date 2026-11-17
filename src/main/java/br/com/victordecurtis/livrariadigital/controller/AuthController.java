package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.service.ApiKeyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.headers.Header;
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

@Tag(name = "Autenticação", description = "Faça login para emitir uma chave X-API-Key. O papel da conta define quais operações administrativas ela pode executar; revogue a chave ao sair.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido; consulte Retry-After e os cabeçalhos de saldo.", headers = @Header(name = "Retry-After", description = "Quantidade de segundos que o cliente deve aguardar antes de tentar novamente.", schema = @Schema(type = "string", example = "60")), content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class)))})
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final ApiKeyService servicoChaves;

    public AuthController(ApiKeyService servicoChaves) {
        this.servicoChaves = servicoChaves;
    }

    @Operation(summary = "Entrar e gerar chave de API", description = "Valida o e-mail e a senha e emite uma chave X-API-Key. A resposta também mostra o papel e o plano definidos pelo servidor. A chave é exibida somente nesta resposta: use-a no Authorize do Swagger para acessar as rotas autorizadas pelo papel da conta.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticação realizada; chave de API emitida."),
            @ApiResponse(responseCode = "401", description = "E-mail ou senha inválidos.", content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        ApiKeyService.ChaveEmitida chaveEmitida = servicoChaves.autenticar(request.email(), request.senha());
        LoginResponse body = new LoginResponse(chaveEmitida.valor(), chaveEmitida.usuarioId(), chaveEmitida.nome(),
                chaveEmitida.papel(), chaveEmitida.plano(),
                "Guarde esta chave. Ela não será exibida novamente; use-a no cabeçalho X-API-Key.");
        return ResponseEntity.ok().header("X-API-Key", chaveEmitida.valor()).body(body);
    }

    @Operation(summary = "Sair e revogar chave", description = "Revoga a chave enviada em X-API-Key. Depois do logout, essa chave deixa de autorizar operações protegidas. Se a chave já estiver revogada ou tiver sido removida com o cadastro do leitor, a operação continua respondendo 204.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Chave revogada."),
            @ApiResponse(responseCode = "401", description = "Cabeçalho X-API-Key ausente.", content = @Content(schema = @Schema(implementation = ResponseError.class)))
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Parameter(description = "Chave emitida pelo endpoint de login.", example = "biblio_chave-emitida-no-login")
            @RequestHeader("X-API-Key") String chave) {
        servicoChaves.revogar(chave);
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
            @Schema(description = "Papel de acesso atribuído pelo servidor.", example = "MEMBRO") br.com.victordecurtis.livrariadigital.model.PapelAcesso papel,
            @Schema(description = "Plano de utilização atribuído pelo servidor.", example = "BASE") br.com.victordecurtis.livrariadigital.model.PlanoUtilizacao plano,
            String mensagem) {
    }
}
