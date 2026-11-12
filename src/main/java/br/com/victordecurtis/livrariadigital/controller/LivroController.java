package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Livro;
import br.com.victordecurtis.livrariadigital.handler.ResponseError;
import br.com.victordecurtis.livrariadigital.service.LivroService;
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
import java.util.List;
import java.util.UUID;

@Tag(name = "Catálogo de livros", description = "Operações de pesquisa, cadastro, consulta, atualização e remoção de livros.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite de 20 requisições por minuto excedido; aguarde o tempo informado em Retry-After.", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class), examples = @ExampleObject(value = "{\"status\":\"error\",\"statusCode\":429,\"error\":\"Too Many Requests\",\"errors\":[\"Limite de 20 requisições por minuto excedido. Aguarde o tempo indicado no cabeçalho Retry-After e tente novamente.\"]}")))})
@RestController
@RequestMapping("/api/livros")
public class LivroController {
    private final LivroService livroService;
    private final IdempotencyService idempotencyService;

    @Autowired
    public LivroController(LivroService livroService, IdempotencyService idempotencyService) {
        this.livroService = livroService;
        this.idempotencyService = idempotencyService;
    }

    @Operation(summary = "Listar livros", description = "Retorna uma página dos livros cadastrados. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=titulo,asc.")
    @GetMapping
    public ResponseEntity<Page<Livro>> listarLivros(
            @ParameterObject @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(livroService.buscarTodos(pageable));
    }

    @Operation(summary = "Consultar livro por UUID", description = "Retorna os dados editoriais do livro, incluindo autor e categoria. Retorna 404 quando não houver livro com o UUID informado.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Livro encontrado."),
            @ApiResponse(responseCode = "404", description = "Não existe livro com esse UUID.")})
    @GetMapping("/{id}")
    public ResponseEntity<Livro> consultarLivro(@Parameter(description = "UUID do livro que será consultado.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        Livro livro = livroService.buscarPorId(id);
        return livro == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(livro);
    }

    @Operation(summary = "Pesquisar livros", description = "Busca livros por palavra ou trecho no título, nas descrições, no nome do autor e na categoria. Informe o termo e, se desejar, use page, size e sort para paginar e ordenar os resultados.")
    @GetMapping("/pesquisa")
    public ResponseEntity<Page<Livro>> pesquisarLivros(@Parameter(description = "Palavra ou trecho a localizar no título, descrições, autor ou categoria.", example = "Duna") @RequestParam("termo") String termo,
            @ParameterObject @PageableDefault(size = 10, sort = "titulo") Pageable pageable) {
        return ResponseEntity.ok(livroService.buscarPorPalavraChave(termo, pageable));
    }

    @Operation(summary = "Cadastrar livro", description = "Cria uma obra no catálogo. Informe os dados editoriais e os UUIDs de um autor e de uma categoria já cadastrados; o ID do livro é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação."),
            @ApiResponse(responseCode = "409", description = "A chave já foi usada, mas o registro correspondente foi removido.")})
    @PostMapping
    public ResponseEntity<Livro> cadastrarLivro(
            @Parameter(description = "Chave única gerada pelo cliente para esta tentativa. Reutilize a mesma chave somente ao repetir a mesma criação.", example = "livro-2026-001")
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody Livro livro) {
        IdempotentCreation<Livro> resultado = idempotencyService.createOnce(
                "livros", idempotencyKey, () -> livroService.salvar(livro),
                Livro::getId, livroService::buscarPorId);
        Livro criado = resultado.value();
        return ResponseEntity.created(URI.create("/api/livros/" + criado.getId()))
                .header("Idempotency-Replayed", Boolean.toString(resultado.replayed())).body(criado);
    }

    @Operation(summary = "Criar ou atualizar livro", description = "Use o UUID da rota para identificar o livro. No corpo, envie todos os campos obrigatórios e os UUIDs de autor e categoria já cadastrados, sem incluir o ID do livro. Se não existir, cria com esse UUID (201); se já existir, substitui os dados (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Livro existente atualizado."),
            @ApiResponse(responseCode = "201", description = "Novo livro criado no catálogo."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PutMapping("/{id}")
    public ResponseEntity<Livro> criarOuAtualizarLivro(@Parameter(description = "UUID que identifica o livro. Se ainda não existir, será usado para criá-lo.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Livro livro) {
        UpsertResult<Livro> resultado = livroService.atualizarOuCriar(id, livro);
        if (resultado.created()) {
            return ResponseEntity.created(URI.create("/api/livros/" + id)).body(resultado.value());
        }
        return ResponseEntity.ok(resultado.value());
    }

    @Operation(summary = "Remover livro", description = "Exclui do catálogo o livro identificado pelo UUID. Em sucesso, responde 204 sem corpo.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Livro removido com sucesso."),
            @ApiResponse(responseCode = "401", description = "Chave X-API-Key ausente ou inválida.", content = @Content(schema = @Schema(implementation = ResponseError.class)))})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLivro(@Parameter(description = "UUID do livro que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        livroService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}








