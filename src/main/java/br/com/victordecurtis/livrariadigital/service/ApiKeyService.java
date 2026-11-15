package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.handler.UnauthorizedException;
import br.com.victordecurtis.livrariadigital.model.ApiKey;
import br.com.victordecurtis.livrariadigital.model.Usuario;
import br.com.victordecurtis.livrariadigital.repository.ApiKeyRepository;
import br.com.victordecurtis.livrariadigital.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class ApiKeyService {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository repositorioLeitores;
    private final ApiKeyRepository repositorioChaves;
    private final PasswordEncoder codificadorSenha;

    public ApiKeyService(UsuarioRepository repositorioLeitores,
                         ApiKeyRepository repositorioChaves,
                         PasswordEncoder codificadorSenha) {
        this.repositorioLeitores = repositorioLeitores;
        this.repositorioChaves = repositorioChaves;
        this.codificadorSenha = codificadorSenha;
    }

    @Transactional
    public ChaveEmitida autenticar(String email, String senha) {
        Usuario usuario = repositorioLeitores.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));
        if (!validarSenha(usuario, senha)) {
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }

        byte[] bytesAleatorios = new byte[32];
        SECURE_RANDOM.nextBytes(bytesAleatorios);
        String chave = "biblio_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytesAleatorios);
        repositorioChaves.save(new ApiKey(hashSha256(chave), usuario));
        return new ChaveEmitida(chave, usuario.getId(), usuario.getNome());
    }

    @Transactional(readOnly = true)
    public boolean chaveValida(String chave) {
        return chave != null && !chave.isBlank()
                && repositorioChaves.findByKeyHashAndRevokedAtIsNull(hashSha256(chave)).isPresent();
    }

    @Transactional
    public void revogar(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new UnauthorizedException("Informe uma chave válida no cabeçalho X-API-Key.");
        }
        repositorioChaves.findByKeyHash(hashSha256(chave)).ifPresent(chaveSalva -> {
            chaveSalva.revogar();
            repositorioChaves.save(chaveSalva);
        });
    }

    private boolean validarSenha(Usuario usuario, String senhaRecebida) {
        String senhaArmazenada = usuario.getSenha();
        if (senhaArmazenada == null || senhaRecebida == null) {
            return false;
        }
        if (ehHashBCrypt(senhaArmazenada)) {
            return codificadorSenha.matches(senhaRecebida, senhaArmazenada);
        }

        boolean senhaLegadaValida = MessageDigest.isEqual(
                senhaRecebida.getBytes(StandardCharsets.UTF_8), senhaArmazenada.getBytes(StandardCharsets.UTF_8));
        if (senhaLegadaValida) {
            usuario.setSenha(codificadorSenha.encode(senhaRecebida));
            repositorioLeitores.save(usuario);
        }
        return senhaLegadaValida;
    }

    private boolean ehHashBCrypt(String valor) {
        return valor.startsWith("$2a$") || valor.startsWith("$2b$") || valor.startsWith("$2y$");
    }

    private String hashSha256(String valor) {
        try {
            byte[] resumo = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(resumo);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM.", exception);
        }
    }

    public record ChaveEmitida(String valor, UUID usuarioId, String nome) {
    }
}
