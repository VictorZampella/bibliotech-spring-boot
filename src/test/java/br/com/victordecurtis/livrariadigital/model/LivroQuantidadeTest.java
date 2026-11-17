package br.com.victordecurtis.livrariadigital.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LivroQuantidadeTest {
    private final Validator validador = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void quantidadeZeroEValida() {
        Livro livro = new Livro();
        livro.setQuantidadeExemplares(0);

        assertFalse(validador.validateProperty(livro, "quantidadeExemplares").stream()
                .anyMatch(violacao -> violacao.getPropertyPath().toString().equals("quantidadeExemplares")));
    }

    @Test
    void quantidadePositivaEValida() {
        Livro livro = new Livro();
        livro.setQuantidadeExemplares(8);

        assertTrue(validador.validateProperty(livro, "quantidadeExemplares").isEmpty());
    }

    @Test
    void quantidadeNegativaERejeitada() {
        Livro livro = new Livro();
        livro.setQuantidadeExemplares(-1);

        assertFalse(validador.validateProperty(livro, "quantidadeExemplares").isEmpty());
    }
}
