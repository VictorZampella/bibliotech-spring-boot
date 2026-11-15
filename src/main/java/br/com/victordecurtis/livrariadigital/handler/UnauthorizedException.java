package br.com.victordecurtis.livrariadigital.handler;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException(String mensagem) {
        super(mensagem);
    }
}
