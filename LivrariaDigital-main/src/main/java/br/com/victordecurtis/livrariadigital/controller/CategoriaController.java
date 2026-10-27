package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Categoria;
import br.com.victordecurtis.livrariadigital.service.CategoriaService;
import br.com.victordecurtis.livrariadigital.service.UpsertResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Categorias e gêneros", description = "Operações para organizar o catálogo por assuntos e gêneros literários.")
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService categoriaService;

    @Autowired
    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(summary = "Listar categorias", description = "Retorna uma página das categorias cadastradas. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=nome,asc.")
    @GetMapping
    public ResponseEntity<Page<Categoria>> listarCategorias(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(categoriaService.buscarTodos(pageable));
    }

    @Operation(summary = "Consultar categoria por UUID", description = "Busca a categoria pelo identificador informado. Retorna 404 quando não houver cadastro com esse UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Categoria encontrada."),
            @ApiResponse(responseCode = "404", description = "Não existe categoria com esse UUID.")})
    @GetMapping("/{id}")
    public ResponseEntity<Categoria> consultarCategoria(@Parameter(description = "UUID da categoria que será consultada.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        Categoria categoria = categoriaService.buscarPorId(id);
        return categoria == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(categoria);
    }

    @Operation(summary = "Cadastrar categoria", description = "Cria uma categoria ou gênero literário. Informe um nome obrigatório e exclusivo; o ID é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Categoria cadastrada com sucesso."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PostMapping
    public ResponseEntity<Categoria> cadastrarCategoria(@Valid @RequestBody Categoria categoria) {
        Categoria criada = categoriaService.salvar(categoria);
        return ResponseEntity.created(URI.create("/api/categorias/" + criada.getId())).body(criada);
    }

    @Operation(summary = "Criar ou atualizar categoria", description = "Use o UUID da rota para identificar a categoria e informe o nome no corpo, sem incluir o ID. Se a categoria não existir, cria com esse UUID (201); se já existir, atualiza o nome (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Categoria existente atualizada."),
            @ApiResponse(responseCode = "201", description = "Nova categoria criada."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PutMapping("/{id}")
    public ResponseEntity<Categoria> criarOuAtualizarCategoria(@Parameter(description = "UUID que identifica a categoria. Se ainda não existir, será usado para criá-la.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Categoria categoria) {
        UpsertResult<Categoria> resultado = categoriaService.atualizarOuCriar(id, categoria);
        if (resultado.created()) {
            return ResponseEntity.created(URI.create("/api/categorias/" + id)).body(resultado.value());
        }
        return ResponseEntity.ok(resultado.value());
    }

    @Operation(summary = "Remover categoria", description = "Exclui do catálogo a categoria identificada pelo UUID. Em sucesso, responde 204 sem corpo.")
    @ApiResponse(responseCode = "204", description = "Categoria removida com sucesso.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerCategoria(@Parameter(description = "UUID da categoria que será removida.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        categoriaService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}




