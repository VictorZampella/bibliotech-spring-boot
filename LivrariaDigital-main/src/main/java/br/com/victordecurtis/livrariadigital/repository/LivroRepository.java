package br.com.victordecurtis.livrariadigital.repository;

import br.com.victordecurtis.livrariadigital.model.Livro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LivroRepository extends JpaRepository<Livro, UUID> {
}
