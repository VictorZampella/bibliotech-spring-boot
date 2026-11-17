package br.com.victordecurtis.livrariadigital.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseError> tratarJsonInvalido(HttpMessageNotReadableException excecao) {
        ResponseError resposta = new ResponseError("Bad Request", 400, List.of(
                "O JSON enviado está inválido, incompleto ou tem um valor incompatível. Confira as chaves, aspas, vírgulas e os formatos esperados."));
        return ResponseEntity.badRequest().body(resposta);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseError> tratarParametroInvalido(MethodArgumentTypeMismatchException excecao) {
        String tipo = UUID.class.equals(excecao.getRequiredType())
                ? "UUID válido" : "valor do tipo " + (excecao.getRequiredType() == null
                ? "esperado" : excecao.getRequiredType().getSimpleName());
        return erro(HttpStatus.BAD_REQUEST, "Bad Request",
                "O parâmetro '" + excecao.getName() + "' precisa conter um " + tipo + ".");
    }

    @ExceptionHandler({MissingRequestHeaderException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ResponseError> tratarCampoObrigatorioAusente(Exception excecao) {
        String campo;
        if (excecao instanceof MissingRequestHeaderException falta) {
            if ("X-API-Key".equalsIgnoreCase(falta.getHeaderName())) {
                return erro(HttpStatus.UNAUTHORIZED, "Unauthorized",
                        "Envie uma chave válida no cabeçalho X-API-Key.");
            }
            campo = "cabeçalho '" + falta.getHeaderName() + "'";
        } else {
            MissingServletRequestParameterException falta = (MissingServletRequestParameterException) excecao;
            campo = "parâmetro '" + falta.getParameterName() + "'";
        }
        return erro(HttpStatus.BAD_REQUEST, "Bad Request", "Informe o " + campo + " obrigatório.");
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<ResponseError> tratarMetodoNaoPermitido(HttpRequestMethodNotSupportedException excecao) {
        return erro(HttpStatus.METHOD_NOT_ALLOWED, "Method Not Allowed",
                "O método " + excecao.getMethod() + " não é aceito nesta rota. Confira o método indicado no Swagger.");
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public ResponseEntity<ResponseError> tratarFormatoNaoAceito(HttpMediaTypeNotSupportedException excecao) {
        return erro(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported Media Type",
                "Formato não aceito. Envie o corpo da requisição como application/json.");
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    @ResponseStatus(HttpStatus.NOT_ACCEPTABLE)
    public ResponseEntity<ResponseError> tratarRespostaNaoAceita(HttpMediaTypeNotAcceptableException excecao) {
        return erro(HttpStatus.NOT_ACCEPTABLE, "Not Acceptable",
                "A API não consegue responder no formato solicitado. Use Accept: application/json.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ResponseError> tratarErroDeValidacao(MethodArgumentNotValidException excecao) {
        List<String> erros = new ArrayList<>();

        for (FieldError erro : excecao.getBindingResult().getFieldErrors()) {
            erros.add(erro.getDefaultMessage());
        }

        for (ObjectError erro : excecao.getBindingResult().getGlobalErrors()) {
            erros.add(erro.getDefaultMessage());
        }

        ResponseError resposta = new ResponseError("Bad Request", 400, erros);
        return ResponseEntity.badRequest().body(resposta);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseError> tratarConflitoDeNegocio(BusinessException excecao) {
        List<String> erros = new ArrayList<>();
        erros.add(excecao.getMessage());

        ResponseError resposta = new ResponseError("Conflict", 409, erros);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(resposta);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ResponseError> tratarConflitoDeDados(DataIntegrityViolationException excecao) {
        return erro(HttpStatus.CONFLICT, "Conflict",
                "O cadastro conflita com um dado existente ou com uma relação obrigatória. Confira os IDs e os valores exclusivos.");
    }

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseError> tratarAcessoNaoAutorizado(UnauthorizedException excecao) {
        ResponseError resposta = new ResponseError("Unauthorized", 401, List.of(excecao.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseError> tratarErroInterno(Exception excecao) {
        logger.error("Erro inesperado ao processar a requisição", excecao);
        return erro(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "Ocorreu um erro inesperado no servidor. Tente novamente mais tarde.");
    }

    private ResponseEntity<ResponseError> erro(HttpStatus status, String nome, String mensagem) {
        ResponseError resposta = new ResponseError(nome, status.value(), List.of(mensagem));
        return ResponseEntity.status(status).body(resposta);
    }
}
