package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Usuario;
import br.com.victordecurtis.livrariadigital.assembler.UsuarioModelAssembler;
import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.service.IdempotencyService;
import br.com.victordecurtis.livrariadigital.service.IdempotentCreation;
import br.com.victordecurtis.livrariadigital.service.UpsertResult;
import br.com.victordecurtis.livrariadigital.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Clientes e leitores", description = "Operações para cadastrar, consultar, atualizar e remover leitores da biblioteca.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido; consulte Retry-After e os cabeçalhos de saldo.", headers = @Header(name = "Retry-After", description = "Quantidade de segundos que o cliente deve aguardar antes de tentar novamente.", schema = @Schema(type = "string", example = "60")), content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class), examples = @ExampleObject(value = "{\"status\":\"error\",\"statusCode\":429,\"error\":\"Too Many Requests\",\"errors\":[\"Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido. Consulte Retry-After e os cabeçalhos de saldo antes de tentar novamente.\"]}")))})
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService servicoLeitores;
    private final IdempotencyService servicoIdempotencia;
    private final UsuarioModelAssembler montador;
    private final PagedResourcesAssembler<Usuario> montadorDePaginas;

    public UsuarioController(UsuarioService servicoLeitores, IdempotencyService servicoIdempotencia,
                             UsuarioModelAssembler montador, PagedResourcesAssembler<Usuario> montadorDePaginas) {
        this.servicoLeitores = servicoLeitores;
        this.servicoIdempotencia = servicoIdempotencia;
        this.montador = montador;
        this.montadorDePaginas = montadorDePaginas;
    }

    @Operation(summary = "Listar leitores", description = "Retorna uma página dos leitores cadastrados. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=nome,asc.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses(@ApiResponse(responseCode = "403", description = "Somente usuários Admin podem consultar leitores."))
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Usuario>>> listarLeitores(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        var leitores = servicoLeitores.buscarTodos(paginacao);
        var link = linkTo(methodOn(UsuarioController.class).listarLeitores(paginacao)).withSelfRel();
        return ResponseEntity.ok(montadorDePaginas.toModel(leitores, montador, link));
    }

    @Operation(summary = "Consultar leitor por UUID", description = "Busca o cadastro do leitor pelo identificador informado. Retorna 404 quando não houver leitor com esse UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cadastro do leitor encontrado."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem consultar leitores."),
            @ApiResponse(responseCode = "404", description = "Não existe leitor com esse UUID.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> consultarLeitor(@Parameter(description = "UUID do leitor que será consultado.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        Usuario usuario = servicoLeitores.buscarPorId(id);
        return usuario == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(montador.toModel(usuario));
    }

    @Operation(summary = "Cadastrar leitor", description = "Cria um cadastro de leitor. Informe nome, e-mail e telefone exclusivos, senha com pelo menos 8 caracteres sem espaços, data de nascimento e CEP. O endereço é completado pelo ViaCEP; o ID é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Leitor cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação."),
            @ApiResponse(responseCode = "409", description = "A chave já foi usada, mas o registro correspondente foi removido.")})
    @PostMapping
    public ResponseEntity<EntityModel<Usuario>> cadastrarLeitor(
            @Parameter(description = "Chave única gerada pelo cliente para esta tentativa. Reutilize a mesma chave somente ao repetir a mesma criação.", example = "leitor-2026-001")
            @RequestHeader("X-Idempotency-Key") String chaveIdempotencia,
            @Valid @RequestBody Usuario usuario) {
        IdempotentCreation<Usuario> resultado = servicoIdempotencia.criarUmaVez(
                "usuarios", chaveIdempotencia, () -> servicoLeitores.salvar(usuario),
                Usuario::getId, servicoLeitores::buscarPorId);
        Usuario criado = resultado.registro();
        return ResponseEntity.created(URI.create("/api/usuarios/" + criado.getId()))
                .header("Idempotency-Replayed", Boolean.toString(resultado.repetida())).body(montador.toModel(criado));
    }

    @Operation(summary = "Criar ou atualizar leitor", description = "Use o UUID da rota para identificar o leitor. No corpo, envie todos os campos obrigatórios do cadastro, sem incluir o ID. Se o leitor não existir, cria com esse UUID (201); se já existir, substitui os dados cadastrais (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cadastro existente atualizado."),
            @ApiResponse(responseCode = "201", description = "Novo cadastro de leitor criado."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem atualizar leitores."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Usuario>> criarOuAtualizarLeitor(@Parameter(description = "UUID que identifica o leitor. Se ainda não existir, será usado para criá-lo.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Usuario usuario) {
        UpsertResult<Usuario> resultado = servicoLeitores.atualizarOuCriar(id, usuario);
        if (resultado.criado()) {
            return ResponseEntity.created(URI.create("/api/usuarios/" + id)).body(montador.toModel(resultado.registro()));
        }
        return ResponseEntity.ok(montador.toModel(resultado.registro()));
    }

    @Operation(summary = "Remover leitor", description = "Exclui o cadastro do leitor identificado pelo UUID. Em sucesso, responde 204 sem corpo; se o leitor não existir, retorna 409.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cadastro removido com sucesso."),
            @ApiResponse(responseCode = "401", description = "Chave X-API-Key ausente ou inválida.", content = @Content(schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem remover leitores."),
            @ApiResponse(responseCode = "409", description = "O leitor informado não foi encontrado.")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLeitor(@Parameter(description = "UUID do leitor que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        servicoLeitores.excluir(id);
        return ResponseEntity.noContent().build();
    }
}




