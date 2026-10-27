package br.com.victordecurtis.livrariadigital.repository;

import br.com.victordecurtis.livrariadigital.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AutorRepository extends JpaRepository<Autor, UUID> {
}
