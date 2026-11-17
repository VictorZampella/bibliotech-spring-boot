package br.com.victordecurtis.livrariadigital.handler;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.MockHttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GlobalExceptionHandlerTest {

    @Test
    void jsonInvalidoRetorna400ComUmaOrientacaoClara() {
        var resposta = new GlobalExceptionHandler().tratarJsonInvalido(
                new HttpMessageNotReadableException("JSON incompleto", new MockHttpInputMessage(new byte[0])));

        assertEquals(HttpStatus.BAD_REQUEST, resposta.getStatusCode());
        assertEquals(400, resposta.getBody().getStatusCode());
        assertTrue(resposta.getBody().getErrors().get(0).contains("JSON enviado está inválido"));
    }
}
