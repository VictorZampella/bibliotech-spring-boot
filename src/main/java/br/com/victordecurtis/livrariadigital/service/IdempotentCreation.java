package br.com.victordecurtis.livrariadigital.service;

public record IdempotentCreation<T>(T registro, boolean repetida) {
}
