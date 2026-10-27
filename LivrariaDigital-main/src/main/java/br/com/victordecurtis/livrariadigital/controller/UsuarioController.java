package br.com.victordecurtis.livrariadigital.controller;

import br.com.victordecurtis.livrariadigital.model.Usuario;
import br.com.victordecurtis.livrariadigital.service.UpsertResult;
import br.com.victordecurtis.livrariadigital.service.UsuarioService;
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

@Tag(name = "Clientes e leitores", description = "Operações para cadastrar, consultar, atualizar e remover leitores da biblioteca.")
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService usuarioService;

    @Autowired
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @Operation(summary = "Listar leitores", description = "Retorna uma página dos leitores cadastrados. Use page (página iniciando em 0), size (itens por página) e sort para ordenar; por exemplo, sort=nome,asc.")
    @GetMapping
    public ResponseEntity<Page<Usuario>> listarLeitores(
            @ParameterObject @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(usuarioService.buscarTodos(pageable));
    }

    @Operation(summary = "Consultar leitor por UUID", description = "Busca o cadastro do leitor pelo identificador informado. Retorna 404 quando não houver leitor com esse UUID.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cadastro do leitor encontrado."),
            @ApiResponse(responseCode = "404", description = "Não existe leitor com esse UUID.")})
    @GetMapping("/{id}")
    public ResponseEntity<Usuario> consultarLeitor(@Parameter(description = "UUID do leitor que será consultado.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return usuario == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(usuario);
    }

    @Operation(summary = "Cadastrar leitor", description = "Cria um cadastro de leitor. Informe nome, e-mail e telefone exclusivos, senha com pelo menos 8 caracteres sem espaços, data de nascimento e CEP. O endereço é completado pelo ViaCEP; o ID é gerado pela API.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Leitor cadastrado com sucesso."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PostMapping
    public ResponseEntity<Usuario> cadastrarLeitor(@Valid @RequestBody Usuario usuario) {
        Usuario criado = usuarioService.salvar(usuario);
        return ResponseEntity.created(URI.create("/api/usuarios/" + criado.getId())).body(criado);
    }

    @Operation(summary = "Criar ou atualizar leitor", description = "Use o UUID da rota para identificar o leitor. No corpo, envie todos os campos obrigatórios do cadastro, sem incluir o ID. Se o leitor não existir, cria com esse UUID (201); se já existir, substitui os dados cadastrais (200).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Cadastro existente atualizado."),
            @ApiResponse(responseCode = "201", description = "Novo cadastro de leitor criado."),
            @ApiResponse(responseCode = "400", description = "Os dados enviados não passaram pela validação.")})
    @PutMapping("/{id}")
    public ResponseEntity<Usuario> criarOuAtualizarLeitor(@Parameter(description = "UUID que identifica o leitor. Se ainda não existir, será usado para criá-lo.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id, @Valid @RequestBody Usuario usuario) {
        UpsertResult<Usuario> resultado = usuarioService.atualizarOuCriar(id, usuario);
        if (resultado.created()) {
            return ResponseEntity.created(URI.create("/api/usuarios/" + id)).body(resultado.value());
        }
        return ResponseEntity.ok(resultado.value());
    }

    @Operation(summary = "Remover leitor", description = "Exclui o cadastro do leitor identificado pelo UUID. Em sucesso, responde 204 sem corpo; se o leitor não existir, retorna 409.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Cadastro removido com sucesso."),
            @ApiResponse(responseCode = "409", description = "O leitor informado não foi encontrado.")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removerLeitor(@Parameter(description = "UUID do leitor que será removido.", example = "c4a7e291-6d53-4a8b-9f10-2b7d5e3a1c64") @PathVariable UUID id) {
        usuarioService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}




