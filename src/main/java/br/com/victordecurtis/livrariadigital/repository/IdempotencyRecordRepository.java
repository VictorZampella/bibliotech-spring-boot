package br.com.victordecurtis.livrariadigital.repository;

import br.com.victordecurtis.livrariadigital.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdempotencyRecordRepository extends JpaRepository<IdempotencyRecord, String> {
}
