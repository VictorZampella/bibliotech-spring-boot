package br.com.victordecurtis.livrariadigital.config;

import br.com.victordecurtis.livrariadigital.service.ApiKeyService;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

final class FakeApiKeyService extends ApiKeyService {
    private final Map<String, Acesso> acessos = new ConcurrentHashMap<>();

    FakeApiKeyService() {
        super(null, null, null, null);
    }

    void registrar(String chave, Acesso acesso) {
        acessos.put(chave, acesso);
    }

    @Override
    public Optional<Acesso> obterAcesso(String chave) {
        return Optional.ofNullable(chave == null ? null : acessos.get(chave));
    }
}
