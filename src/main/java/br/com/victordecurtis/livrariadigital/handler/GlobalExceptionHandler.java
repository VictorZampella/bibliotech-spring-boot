package br.com.victordecurtis.livrariadigital.handler;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ResponseError> tratarAcessoNaoAutorizado(UnauthorizedException excecao) {
        ResponseError resposta = new ResponseError("Unauthorized", 401, List.of(excecao.getMessage()));
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resposta);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ResponseError> tratarErroInterno(Exception excecao) {
        List<String> erros = new ArrayList<>();
        erros.add(excecao.getMessage());

        ResponseError resposta = new ResponseError("Internal Server Error", 500, erros);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resposta);
    }
}
