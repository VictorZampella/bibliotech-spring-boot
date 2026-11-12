package br.com.victordecurtis.livrariadigital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

/** Persiste somente o hash da chave de API; o valor original é mostrado uma única vez no login. */
@Entity
@Table(name = "api_keys")
public class ApiKey {
    @Id
    private UUID id;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    protected ApiKey() {
    }

    public ApiKey(String keyHash, Usuario usuario) {
        this.id = UUID.randomUUID();
        this.keyHash = keyHash;
        this.usuario = usuario;
        this.createdAt = Instant.now();
    }

    public String getKeyHash() {
        return keyHash;
    }

    public Instant getRevokedAt() {
        return revokedAt;
    }

    public void revoke() {
        if (revokedAt == null) {
            revokedAt = Instant.now();
        }
    }
}
