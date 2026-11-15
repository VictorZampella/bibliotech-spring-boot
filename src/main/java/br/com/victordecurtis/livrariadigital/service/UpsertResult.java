package br.com.victordecurtis.livrariadigital.service;

public record UpsertResult<T>(T registro, boolean criado) {
}
