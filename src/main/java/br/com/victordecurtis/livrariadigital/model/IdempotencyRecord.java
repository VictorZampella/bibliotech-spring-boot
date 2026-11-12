package br.com.victordecurtis.livrariadigital.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

/** Guarda a chave de uma criação já processada para evitar duplicidade em retries. */
@Entity
@Table(name = "api_idempotency")
public class IdempotencyRecord {
    @Id
    @Column(name = "idempotency_key", length = 260, nullable = false)
    private String idempotencyKey;

    @Column(name = "resource_name", length = 50, nullable = false)
    private String resource;

    @Column(name = "resource_id", nullable = false)
    private UUID resourceId;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, String resource, UUID resourceId) {
        this.idempotencyKey = idempotencyKey;
        this.resource = resource;
        this.resourceId = resourceId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getResource() {
        return resource;
    }

    public UUID getResourceId() {
        return resourceId;
    }
}
