package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Livro;
import br.com.victordecurtis.livrariadigital.assembler.LivroModelAssembler;
import br.com.victordecurtis.livrariadigital.dto.LivroVersao2Response;
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
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Tag(name = "Catálogo de livros", description = "Operações de pesquisa, cadastro, consulta, atualização e remoção de livros.")
@ApiResponses({@ApiResponse(responseCode = "429", description = "Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido; consulte Retry-After e os cabeçalhos de saldo.", headers = @Header(name = "Retry-After", description = "Quantidade de segundos que o cliente deve aguardar antes de tentar novamente.", schema = @Schema(type = "string", example = "60")), content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseError.class), examples = @ExampleObject(value = "{\"status\":\"error\",\"statusCode\":429,\"error\":\"Too Many Requests\",\"errors\":[\"Limite por IP (120 por minuto) ou pela chave (30 no BASE, 100 no PREMIUM) excedido. Consulte Retry-After e os cabeçalhos de saldo antes de tentar novamente.\"]}")))})
@RestController
@RequestMapping("/api/livros")
public class LivroController {
    private final LivroService servicoLivros;
    private final IdempotencyService servicoIdempotencia;
    private final LivroModelAssembler montador;
    private final PagedResourcesAssembler<Livro> montadorDePaginas;

    public LivroController(LivroService servicoLivros, IdempotencyService servicoIdempotencia,
                           LivroModelAssembler montador, PagedResourcesAssembler<Livro> montadorDePaginas) {
        this.servicoLivros = servicoLivros;
        this.servicoIdempotencia = servicoIdempotencia;
        this.montador = montador;
        this.montadorDePaginas = montadorDePaginas;
    }

    @Operation(summary = "Listar livros", description = "Retorna uma página dos livros cadastrados. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=titulo,asc.")
    @GetMapping
    public ResponseEntity<PagedModel<EntityModel<Livro>>> listarLivros(
            @ParameterObject @PageableDefault(size = 10, sort = "titulo") Pageable paginacao) {
        var livros = servicoLivros.buscarTodos(paginacao);
        var link = linkTo(methodOn(LivroController.class).listarLivros(paginacao)).withSelfRel();
        return ResponseEntity.ok(montadorDePaginas.toModel(livros, montador, link));
    }

    @Operation(summary = "Consultar livro por UUID", description = "Retorna os dados editoriais do livro com links para consultar, atualizar ou remover o livro e acessar seu autor e categoria. A versão 1 é usada por padrão e preserva os objetos completos de autor e categoria. Envie X-API-Version: 2 para receber uma representação resumida com os nomes do autor e da categoria.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Livro encontrado na versão solicitada."),
            @ApiResponse(responseCode = "400", description = "Versão não suportada. Use 1 ou 2."),
            @ApiResponse(responseCode = "404", description = "Não existe livro com esse UUID.")})
    @GetMapping("/{id}")
    public ResponseEntity<?> consultarLivro(
            @Parameter(description = "UUID do livro que será consultado.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id,
            @Parameter(description = "Versão da representação desejada. Se não informar, será usada a versão 1.", example = "2")
            @RequestHeader(name = "X-API-Version", required = false, defaultValue = "1") String versao) {
        if (!versao.equals("1") && !versao.equals("2")) {
            return ResponseEntity.badRequest().body(new ResponseError("Bad Request", 400,
                    List.of("Versão não suportada. Use X-API-Version: 1 ou X-API-Version: 2.")));
        }
        Livro livro = servicoLivros.buscarPorId(id);
        if (livro == null) {
            return ResponseEntity.notFound().build();
        }
        return "2".equals(versao)
                ? ResponseEntity.ok(montador.toModelVersao2(livro))
                : ResponseEntity.ok(montador.toModel(livro));
    }

    @Operation(summary = "Pesquisar livros", description = "Busca livros por palavra ou trecho no título, nas descrições, no nome do autor e na categoria. Informe o termo e, se desejar, use page, size e sort para paginar e ordenar os resultados.")
    @GetMapping("/pesquisa")
    public ResponseEntity<PagedModel<EntityModel<Livro>>> pesquisarLivros(@Parameter(description = "Palavra ou trecho a localizar no título, descrições, autor ou categoria.", example = "Duna") @RequestParam("termo") String termo,
            @ParameterObject @PageableDefault(size = 10, sort = "titulo") Pageable paginacao) {
        var livros = servicoLivros.buscarPorPalavraChave(termo, paginacao);
        var link = linkTo(methodOn(LivroController.class).pesquisarLivros(termo, paginacao)).withSelfRel();
        return ResponseEntity.ok(montadorDePaginas.toModel(livros, montador, link));
    }

    @Operation(summary = "Cadastrar livro", description = "Cria uma obra no catálogo. Informe os dados editoriais e os UUIDs de um autor e de uma categoria já cadastrados; o ID do livro é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Livro cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, JSON malformado/incompleto ou valor em formato incorreto."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem cadastrar livros."),
            @ApiResponse(responseCode = "409", description = "A chave já foi usada, mas o registro correspondente foi removido.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @PostMapping
    public ResponseEntity<EntityModel<Livro>> cadastrarLivro(
            @Parameter(description = "Chave única gerada pelo cliente para esta tentativa. Reutilize a mesma chave somente ao repetir a mesma criação.", example = "livro-2026-001")
            @RequestHeader("X-Idempotency-Key") String chaveIdempotencia,
            @Valid @RequestBody Livro livro) {
        IdempotentCreation<Livro> resultado = servicoIdempotencia.criarUmaVez(
                "livros", chaveIdempotencia, () -> servicoLivros.salvar(livro),
                Livro::getId, servicoLivros::buscarPorId);
        Livro criado = resultado.registro();
        return ResponseEntity.created(URI.create("/api/livros/" + criado.getId()))
                .header("Idempotency-Replayed", Boolean.toString(resultado.repetida())).body(montador.toModel(criado));
    }

    @Operation(summary = "Criar ou atualizar livro", description = "Use o UUID da rota para identificar o livro. No corpo, envie todos os campos obrigatórios e os UUIDs de autor e categoria já cadastrados, sem incluir o ID do livro. Se não existir, cria com esse UUID (201); se já existir, substitui os dados (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Livro existente atualizado."),
            @ApiResponse(responseCode = "201", description = "Novo livro criado no catálogo."),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem atualizar livros."),
            @ApiResponse(responseCode = "400", description = "Dados inválidos, JSON malformado/incompleto ou valor em formato incorreto.")})
    @SecurityRequirement(name = "ApiKeyAuth")
    @PutMapping("/{id}")
    public ResponseEntity<EntityModel<Livro>> criarOuAtualizarLivro(@Parameter(description = "UUID que identifica o livro. Se ainda não existir, será usado para criá-lo.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Livro livro) {
        UpsertResult<Livro> resultado = servicoLivros.atualizarOuCriar(id, livro);
        if (resultado.criado()) {
            return ResponseEntity.created(URI.create("/api/livros/" + id)).body(montador.toModel(resultado.registro()));
        }
        return ResponseEntity.ok(montador.toModel(resultado.registro()));
    }

    @Operation(summary = "Remover livro", description = "Exclui do catálogo o livro identificado pelo UUID. Em sucesso, responde 204 sem corpo.")
    @SecurityRequirement(name = "ApiKeyAuth")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Livro removido com sucesso."),
            @ApiResponse(responseCode = "401", description = "Chave X-API-Key ausente ou inválida.", content = @Content(schema = @Schema(implementation = ResponseError.class))),
            @ApiResponse(responseCode = "403", description = "Somente usuários Admin podem remover livros.")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLivro(@Parameter(description = "UUID do livro que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        servicoLivros.excluir(id);
        return ResponseEntity.noContent().build();
    }
}








