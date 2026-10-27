package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Autor;
import br.com.victordecurtis.livrariadigital.service.AutorService;
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

@Tag(name = "Autores", description = "Operações para cadastrar, consultar, atualizar e remover autores do catálogo.")
@RestController
@RequestMapping("/api/autores")
public class AutorController {
    private final AutorService autorService;

    @Autowired
    public AutorController(AutorService autorService) {
        this.autorService = autorService;
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
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PostMapping
    public ResponseEntity<Autor> cadastrarAutor(@Valid @RequestBody Autor autor) {
        Autor criado = autorService.salvar(autor);
        return ResponseEntity.created(URI.create("/api/autores/" + criado.getId())).body(criado);
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
    @ApiResponse(responseCode = "204", description = "Autor removido com sucesso.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerAutor(@Parameter(description = "UUID do autor que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        autorService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}




