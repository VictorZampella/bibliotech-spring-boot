package br.com.victordecurtis.livrariadigital.repository;

import br.com.victordecurtis.livrariadigital.model.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ApiKeyRepository extends JpaRepository<ApiKey, UUID> {
    Optional<ApiKey> findByKeyHashAndRevokedAtIsNull(String keyHash);
    Optional<ApiKey> findByKeyHash(String keyHash);
}
