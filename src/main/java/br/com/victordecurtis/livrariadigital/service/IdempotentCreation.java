package br.com.victordecurtis.livrariadigital.service;

/** Resultado da criação, indicando se a resposta veio de uma repetição da mesma chave. */
public record IdempotentCreation<T>(T value, boolean replayed) {
}
