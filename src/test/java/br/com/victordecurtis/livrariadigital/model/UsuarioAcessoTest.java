package br.com.victordecurtis.livrariadigital.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UsuarioAcessoTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void clienteNaoPodeDefinirPapelOuPlanoNoCadastro() throws Exception {
        Usuario usuario = objectMapper.readValue("""
                {"papel":"ADMIN","plano":"PREMIUM"}
                """, Usuario.class);

        assertEquals(PapelAcesso.MEMBRO, usuario.getPapel());
        assertEquals(PlanoUtilizacao.BASE, usuario.getPlano());
    }
}
