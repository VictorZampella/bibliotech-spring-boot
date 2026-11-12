package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.handler.BusinessException;
import br.com.victordecurtis.livrariadigital.model.IdempotencyRecord;
import br.com.victordecurtis.livrariadigital.repository.IdempotencyRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class IdempotencyService {
    private static final int MAX_KEY_LENGTH = 200;

    private final IdempotencyRecordRepository repository;

    public IdempotencyService(IdempotencyRecordRepository repository) {
        this.repository = repository;
    }

    /**
     * Executa e registra uma criação na mesma transação. A chave é separada por recurso,
     * para que, por exemplo, a chave "abc" de um autor não conflite com a de um livro.
     */
    @Transactional
    public synchronized <T> IdempotentCreation<T> createOnce(
            String resource,
            String idempotencyKey,
            Supplier<T> creation,
            Function<T, UUID> idExtractor,
            Function<UUID, T> resourceFinder) {
        validateKey(idempotencyKey);
        String scopedKey = resource + ":" + idempotencyKey;

        IdempotencyRecord existingRecord = repository.findById(scopedKey).orElse(null);
        if (existingRecord != null) {
            T existingResource = resourceFinder.apply(existingRecord.getResourceId());
            if (existingResource == null) {
                throw new BusinessException("Esta chave de idempotência já foi usada, mas o cadastro foi removido. Envie uma nova chave.");
            }
            return new IdempotentCreation<>(existingResource, true);
        }

        T createdResource = creation.get();
        UUID createdId = idExtractor.apply(createdResource);
        repository.saveAndFlush(new IdempotencyRecord(scopedKey, resource, createdId));
        return new IdempotentCreation<>(createdResource, false);
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException("Informe o cabeçalho X-Idempotency-Key com uma chave única.");
        }
        if (key.length() > MAX_KEY_LENGTH) {
            throw new BusinessException("A chave X-Idempotency-Key deve ter no máximo 200 caracteres.");
        }
    }
}
