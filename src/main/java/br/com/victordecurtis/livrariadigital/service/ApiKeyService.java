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

    private final UsuarioRepository usuarioRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final PasswordEncoder passwordEncoder;

    public ApiKeyService(UsuarioRepository usuarioRepository,
                         ApiKeyRepository apiKeyRepository,
                         PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.apiKeyRepository = apiKeyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public IssuedApiKey login(String email, String password) {
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email.trim())
                .orElseThrow(() -> new UnauthorizedException("E-mail ou senha inválidos."));
        if (!validarSenha(usuario, password)) {
            throw new UnauthorizedException("E-mail ou senha inválidos.");
        }

        byte[] randomBytes = new byte[32];
        SECURE_RANDOM.nextBytes(randomBytes);
        String rawKey = "biblio_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        apiKeyRepository.save(new ApiKey(sha256(rawKey), usuario));
        return new IssuedApiKey(rawKey, usuario.getId(), usuario.getNome());
    }

    @Transactional(readOnly = true)
    public boolean isValid(String rawKey) {
        return rawKey != null && !rawKey.isBlank()
                && apiKeyRepository.findByKeyHashAndRevokedAtIsNull(sha256(rawKey)).isPresent();
    }

    @Transactional
    public void logout(String rawKey) {
        if (rawKey == null || rawKey.isBlank()) {
            throw new UnauthorizedException("Informe uma chave válida no cabeçalho X-API-Key.");
        }
        apiKeyRepository.findByKeyHash(sha256(rawKey)).ifPresent(apiKey -> {
            apiKey.revoke();
            apiKeyRepository.save(apiKey);
        });
    }

    private boolean validarSenha(Usuario usuario, String rawPassword) {
        String storedPassword = usuario.getSenha();
        if (storedPassword == null || rawPassword == null) {
            return false;
        }
        if (isBcryptHash(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }

        boolean legacyPasswordMatches = MessageDigest.isEqual(
                rawPassword.getBytes(StandardCharsets.UTF_8), storedPassword.getBytes(StandardCharsets.UTF_8));
        if (legacyPasswordMatches) {
            usuario.setSenha(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuario);
        }
        return legacyPasswordMatches;
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 não está disponível nesta JVM.", exception);
        }
    }

    public record IssuedApiKey(String key, UUID userId, String name) {
    }
}
