package br.com.victordecurtis.livrariadigital.service;

public record UpsertResult<T>(T value, boolean created) {
}
