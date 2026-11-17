package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.assembler.AutorModelAssembler;
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

@Tag(name = "Autores", description = "Operações para cadastrar, consultar, atualizar e remover autores do catálogo.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido; consulte Retry-After e os cabeçalhos de saldo.", headers = @Header(name = "Retry-After", description = "Quantidade de segundos que o cliente deve aguardar antes de tentar novamente.", schema = @Schema(type = "string", example = "60")), content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class), examples = @ExampleObject(value = "{\"status\":\"error\",\"statusCode\":429,\"error\":\"Too Many Requests\",\"errors\":[\"Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido. Consulte Retry-After e os cabeçalhos de saldo antes de tentar novamente.\"]}")))})
@RestController
@RequestMapping("/api/autores")
public class AutorController {
    private final AutorService servicoAutores;
    private final IdempotencyService servicoIdempotencia;
    private final AutorModelAssembler montador;
    private final PagedResourcesAssembler<Autor> montadorDePaginas;

    public AutorController(AutorService servicoAutores, IdempotencyService servicoIdempotencia,
                           AutorModelAssembler montador, PagedResourcesAssembler<Autor> montadorDePaginas) {
        this.servicoAutores = servicoAutores;
        this.servicoIdempotencia = servicoIdempotencia;
        this.montador = montador;
        this.montadorDePaginas = montadorDePaginas;
    }

    @Operation(summary = "Listar autores", description = "Retorna uma página dos autores cadastrados. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=nome,asc.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses(@ApiResponse(responseCode = "403", description = "Somente usuários Admin podem consultar autores."))
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Autor>>> listarAutores(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable paginacao) {
        var autores = servicoAutores.buscarTodos(paginacao);
        var link = linkTo(methodOn(AutorController.class).listarAutores(paginacao)).withSelfRel();
        return ResponseEntity.ok(montadorDePaginas.toModel(autores, montador, link));
    }

    @Operation(summary = "Consultar autor por UUID", description = "Busca os dados biográficos do autor pelo identificador informado. Retorna 404 quando não houver autor com esse UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Autor encontrado."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem consultar autores."),
            @ApiResponse(responseCode = "404", description = "Não existe autor com esse UUID.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @GetMapping("/{id}")
    public ResponseEntity<EntityModel<Autor>> consultarAutor(@Parameter(description = "UUID do autor que será consultado.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        Autor autor = servicoAutores.buscarPorId(id);
        return autor == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(montador.toModel(autor));
    }

    @Operation(summary = "Cadastrar autor", description = "Cria um perfil de autor. Informe nome, data de nascimento no formato AAAA-MM-DD, biografia, URL pública da foto e nacionalidade. O ID é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Autor cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, JSON malformado/incompleto ou valor em formato incorreto."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem cadastrar autores."),
            @ApiResponse(responseCode = "409", description = "A chave já foi usada, mas o registro correspondente foi removido.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @PostMapping
    public ResponseEntity<EntityModel<Autor>> cadastrarAutor(
            @Parameter(description = "Chave única gerada pelo cliente para esta tentativa. Reutilize a mesma chave somente ao repetir a mesma criação.", example = "autor-2026-001")
            @RequestHeader("X-Idempotency-Key") String chaveIdempotencia,
            @Valid @RequestBody Autor autor) {
        IdempotentCreation<Autor> resultado = servicoIdempotencia.criarUmaVez(
                "autores", chaveIdempotencia, () -> servicoAutores.salvar(autor),
                Autor::getId, servicoAutores::buscarPorId);
        Autor criado = resultado.registro();
        return ResponseEntity.created(URI.create("/api/autores/" + criado.getId()))
                .header("Idempotency-Replayed", Boolean.toString(resultado.repetida())).body(montador.toModel(criado));
    }

    @Operation(summary = "Criar ou atualizar autor", description = "Use o UUID da rota para identificar o autor. No corpo, envie todos os campos obrigatórios do perfil, sem incluir o ID. Se o autor não existir, cria o perfil (201); se já existir, substitui seus dados (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Perfil existente atualizado."),
            @ApiResponse(responseCode = "201", description = "Novo perfil de autor criado."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem atualizar autores."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, JSON malformado/incompleto ou valor em formato incorreto.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Autor>> criarOuAtualizarAutor(@Parameter(description = "UUID que identifica o autor. Se ainda não existir, será usado para criá-lo.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Autor autor) {
        UpsertResult<Autor> resultado = servicoAutores.atualizarOuCriar(id, autor);
        if (resultado.criado()) {
            return ResponseEntity.created(URI.create("/api/autores/" + id)).body(montador.toModel(resultado.registro()));
        }
        return ResponseEntity.ok(montador.toModel(resultado.registro()));
    }

    @Operation(summary = "Remover autor", description = "Exclui do catálogo o autor identificado pelo UUID. Em sucesso, responde 204 sem corpo.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Autor removido com sucesso."),
            @ApiResponse(responseCode = "401", description = "Chave X-API-Key ausente ou inválida.", content = @Content(schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem remover autores.")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAutor(@Parameter(description = "UUID do autor que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        servicoAutores.excluir(id);
        return ResponseEntity.noContent().build();
    }
}




