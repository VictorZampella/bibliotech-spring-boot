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

    private final IdempotencyRecordRepository repositorio;

    public IdempotencyService(IdempotencyRecordRepository repositorio) {
        this.repositorio = repositorio;
    }

    /**
     * A chave é combinada com o tipo de recurso, evitando conflito entre cadastros diferentes.
     */
    @Transactional
    public synchronized <T> IdempotentCreation<T> criarUmaVez(
            String recurso,
            String chaveIdempotencia,
            Supplier<T> criar,
            Function<T, UUID> extrairId,
            Function<UUID, T> buscarRecurso) {
        validarChave(chaveIdempotencia);
        String chaveComEscopo = recurso + ":" + chaveIdempotencia;

        IdempotencyRecord registroExistente = repositorio.findById(chaveComEscopo).orElse(null);
        if (registroExistente != null) {
            T recursoExistente = buscarRecurso.apply(registroExistente.getResourceId());
            if (recursoExistente == null) {
                throw new BusinessException("Esta chave de idempotência já foi usada, mas o cadastro foi removido. Envie uma nova chave.");
            }
            return new IdempotentCreation<>(recursoExistente, true);
        }

        T recursoCriado = criar.get();
        UUID idCriado = extrairId.apply(recursoCriado);
        repositorio.saveAndFlush(new IdempotencyRecord(chaveComEscopo, recurso, idCriado));
        return new IdempotentCreation<>(recursoCriado, false);
    }

    private void validarChave(String chave) {
        if (chave == null || chave.isBlank()) {
            throw new BusinessException("Informe o cabeçalho X-Idempotency-Key com uma chave única.");
        }
        if (chave.length() > MAX_KEY_LENGTH) {
            throw new BusinessException("A chave X-Idempotency-Key deve ter no máximo 200 caracteres.");
        }
    }
}
