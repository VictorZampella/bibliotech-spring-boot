package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.handler.BusinessException;
import br.com.victordecurtis.livrariadigital.model.Endereco;
import br.com.victordecurtis.livrariadigital.model.Usuario;
import br.com.victordecurtis.livrariadigital.repository.EnderecoRepository;
import br.com.victordecurtis.livrariadigital.repository.ApiKeyRepository;
import br.com.victordecurtis.livrariadigital.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository repositorioLeitores;
    private final EnderecoRepository repositorioEnderecos;
    private final ViaCepService servicoViaCep;
    private final PasswordEncoder codificadorSenha;
    private final ApiKeyRepository repositorioChaves;

    public UsuarioService(UsuarioRepository repositorioLeitores,
                          EnderecoRepository repositorioEnderecos,
                          ViaCepService servicoViaCep,
                          PasswordEncoder codificadorSenha,
                          ApiKeyRepository repositorioChaves) {
        this.repositorioLeitores = repositorioLeitores;
        this.repositorioEnderecos = repositorioEnderecos;
        this.servicoViaCep = servicoViaCep;
        this.codificadorSenha = codificadorSenha;
        this.repositorioChaves = repositorioChaves;
    }

    public Page<Usuario> buscarTodos(Pageable paginacao) {
        return repositorioLeitores.findAll(paginacao);
    }

    public Usuario buscarPorId(UUID id) {
        return repositorioLeitores.findById(id).orElse(null);
    }

    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(UUID.randomUUID());
        }
        codificarSenha(usuario);
        salvarComCep(usuario);
        return usuario;
    }

    @Transactional
    public void excluir(UUID id) {
        Usuario usuario = repositorioLeitores.findById(id).orElse(null);
        if (usuario == null) {
            throw new BusinessException("Leitor não encontrado");
        }
        repositorioChaves.deleteAllByUsuario_Id(id);
        repositorioLeitores.delete(usuario);
    }

    public UpsertResult<Usuario> atualizarOuCriar(UUID id, Usuario dados) {
        Usuario usuario = repositorioLeitores.findById(id).orElse(null);
        boolean criado = usuario == null;

        if (criado) {
            usuario = dados;
            usuario.setId(id);
        } else {
            usuario.setNome(dados.getNome());
            usuario.setEmail(dados.getEmail());
            usuario.setTelefone(dados.getTelefone());
            usuario.setSenha(dados.getSenha());
            usuario.setDataNascimento(dados.getDataNascimento());
            usuario.setEndereco(dados.getEndereco());
        }

        codificarSenha(usuario);
        salvarComCep(usuario);
        return new UpsertResult<>(usuario, criado);
    }

    private void codificarSenha(Usuario usuario) {
        String senha = usuario.getSenha();
        if (senha != null && !senha.startsWith("$2a$") && !senha.startsWith("$2b$") && !senha.startsWith("$2y$")) {
            usuario.setSenha(codificadorSenha.encode(senha));
        }
    }

    private void salvarComCep(Usuario usuario) {
        String cep = usuario.getEndereco().getCep();
        Endereco endereco = repositorioEnderecos.findById(cep).orElseGet(() -> {
            Endereco novoEndereco = servicoViaCep.consultarCep(cep);
            // O ViaCEP retorna o CEP formatado com hífen; a API mantém o valor normalizado.
            novoEndereco.setCep(cep);
            return repositorioEnderecos.save(novoEndereco);
        });
        usuario.setEndereco(endereco);
        repositorioLeitores.save(usuario);
    }
}

