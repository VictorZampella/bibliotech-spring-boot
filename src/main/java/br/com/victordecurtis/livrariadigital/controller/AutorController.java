package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.service.AutorService;
import br.com.victordecurtis.livrariadigital.service.IdempotencyService;
import br.com.victordecurtis.livrariadigital.service.IdempotentCreation;
import br.com.victordecurtis.livrariadigital.service.UpsertResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Autores", description = "Operações para cadastrar, consultar, atualizar e remover autores do catálogo.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite de 20 requisições por minuto excedido; aguarde o tempo informado em Retry-After.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class), examples = @ExampleObject(value = "{\"status\":\"error\",\"statusCode\":429,\"error\":\"Too Many Requests\",\"errors\":[\"Limite de 20 requisições por minuto excedido. Aguarde o tempo indicado no cabeçalho Retry-After e tente novamente.\"]}")))})
@RestController
@RequestMapping("/api/autores")
public class AutorController {
    private final AutorService autorService;
    private final IdempotencyService idempotencyService;

    @Autowired
    public AutorController(AutorService autorService, IdempotencyService idempotencyService) {
        this.autorService = autorService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "Listar autores", description = "Retorna uma página dos autores cadastrados. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=nome,asc.")
    @GetMapping
    public ResponseEntity<Page<Autor>> listarAutores(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(autorService.buscarTodos(pageable));
    }

    @Operation(summary = "Consultar autor por UUID", description = "Busca os dados biográficos do autor pelo identificador informado. Retorna 404 quando não houver autor com esse UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Autor encontrado."),
            @ApiResponse(responseCode = "404", description = "Não existe autor com esse UUID.")})
    @GetMapping("/{id}")
    public ResponseEntity<Autor> consultarAutor(@Parameter(description = "UUID do autor que será consultado.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        Autor autor = autorService.buscarPorId(id);
        return autor == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(autor);
    }

    @Operation(summary = "Cadastrar autor", description = "Cria um perfil de autor. Informe nome, data de nascimento no formato AAAA-MM-DD, biografia, URL pública da foto e nacionalidade. O ID é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Autor cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação."),
            @ApiResponse(responseCode = "409", description = "A chave já foi usada, mas o registro correspondente foi removido.")})
    @PostMapping
    public ResponseEntity<Autor> cadastrarAutor(
            @Parameter(description = "Chave única gerada pelo cliente para esta tentativa. Reutilize a mesma chave somente ao repetir a mesma criação.", example = "autor-2026-001")
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody Autor autor) {
        IdempotentCreation<Autor> resultado = idempotencyService.createOnce(
                "autores", idempotencyKey, () -> autorService.salvar(autor),
                Autor::getId, autorService::buscarPorId);
        Autor criado = resultado.value();
        return ResponseEntity.created(URI.create("/api/autores/" + criado.getId()))
                .header("Idempotency-Replayed", Boolean.toString(resultado.replayed())).body(criado);
    }

    @Operation(summary = "Criar ou atualizar autor", description = "Use o UUID da rota para identificar o autor. No corpo, envie todos os campos obrigatórios do perfil, sem incluir o ID. Se o autor não existir, cria o perfil (201); se já existir, substitui seus dados (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Perfil existente atualizado."),
            @ApiResponse(responseCode = "201", description = "Novo perfil de autor criado."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PutMapping("/{id}")
    public ResponseEntity<Autor> criarOuAtualizarAutor(@Parameter(description = "UUID que identifica o autor. Se ainda não existir, será usado para criá-lo.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Autor autor) {
        UpsertResult<Autor> resultado = autorService.atualizarOuCriar(id, autor);
        if (resultado.created()) {
            return ResponseEntity.created(URI.create("/api/autores/" + id)).body(resultado.value());
        }
        return ResponseEntity.ok(resultado.value());
    }

    @Operation(summary = "Remover autor", description = "Exclui do catálogo o autor identificado pelo UUID. Em sucesso, responde 204 sem corpo.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Autor removido com sucesso."),
            @ApiResponse(responseCode = "401", description = "Chave X-API-Key ausente ou inválida.", content = @Content(schema = @Schema(implementation = ResponseError.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAutor(@Parameter(description = "UUID do autor que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        autorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}




