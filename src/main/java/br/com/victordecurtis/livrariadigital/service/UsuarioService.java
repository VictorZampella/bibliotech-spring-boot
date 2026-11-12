package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.handler.BusinessException;
import br.com.victordecurtis.livrariadigital.model.Endereco;
import br.com.victordecurtis.livrariadigital.model.Usuario;
import br.com.victordecurtis.livrariadigital.repository.EnderecoRepository;
import br.com.victordecurtis.livrariadigital.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final EnderecoRepository enderecoRepository;
    private final ViaCepService viaCepService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository, EnderecoRepository enderecoRepository, ViaCepService viaCepService, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.enderecoRepository = enderecoRepository;
        this.viaCepService = viaCepService;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<Usuario> buscarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    public Usuario buscarPorId(UUID id) {
        return usuarioRepository.findById(id).orElse(null);
    }

    public Usuario salvar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(UUID.randomUUID());
        }
        codificarSenha(usuario);
        salvarComCep(usuario);
        return usuario;
    }

    public void excluir(UUID id) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
        if (usuario == null) {
            throw new BusinessException("Leitor não encontrado");
        }
        usuarioRepository.delete(usuario);
    }

    public UpsertResult<Usuario> atualizarOuCriar(UUID id, Usuario dados) {
        Usuario usuario = usuarioRepository.findById(id).orElse(null);
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
            usuario.setSenha(passwordEncoder.encode(senha));
        }
    }

    private void salvarComCep(Usuario usuario) {
        String cep = usuario.getEndereco().getCep();
        Endereco endereco = enderecoRepository.findById(cep).orElseGet(() -> {
            Endereco novoEndereco = viaCepService.consultarCep(cep);
            // O ViaCEP retorna o CEP formatado com hífen; a API mantém o valor normalizado.
            novoEndereco.setCep(cep);
            return enderecoRepository.save(novoEndereco);
        });
        usuario.setEndereco(endereco);
        usuarioRepository.save(usuario);
    }
}

