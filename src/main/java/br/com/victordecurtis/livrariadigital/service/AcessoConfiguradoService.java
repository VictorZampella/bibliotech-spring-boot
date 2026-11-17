package br.com.victordecurtis.livrariadigital.service;

import br.com.victordecurtis.livrariadigital.model.PapelAcesso;
import br.com.victordecurtis.livrariadigital.model.PlanoUtilizacao;
import br.com.victordecurtis.livrariadigital.model.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AcessoConfiguradoService {
    private final Set<UUID> administradores;
    private final Set<UUID> premium;

    public AcessoConfiguradoService(
            @Value("${bibliotech.access.admin-user-ids:}") String idsAdministradores,
            @Value("${bibliotech.access.premium-user-ids:}") String idsPremium) {
        administradores = converterIds(idsAdministradores);
        premium = converterIds(idsPremium);
    }

    public boolean aplicarConfiguracao(Usuario usuario) {
        PapelAcesso papel = administradores.isEmpty()
                ? valorOuPadrao(usuario.getPapel(), PapelAcesso.MEMBRO)
                : administradores.contains(usuario.getId()) ? PapelAcesso.ADMIN : PapelAcesso.MEMBRO;
        PlanoUtilizacao plano = premium.isEmpty()
                ? valorOuPadrao(usuario.getPlano(), PlanoUtilizacao.BASE)
                : premium.contains(usuario.getId()) ? PlanoUtilizacao.PREMIUM : PlanoUtilizacao.BASE;

        boolean mudou = usuario.getPapel() != papel || usuario.getPlano() != plano;
        usuario.setPapel(papel);
        usuario.setPlano(plano);
        return mudou;
    }

    private <T> T valorOuPadrao(T valor, T padrao) {
        return valor == null ? padrao : valor;
    }

    private Set<UUID> converterIds(String valores) {
        if (valores == null || valores.isBlank()) return Set.of();
        return Arrays.stream(valores.split(","))
                .map(String::trim)
                .filter(valor -> !valor.isEmpty())
                .map(UUID::fromString)
                .collect(Collectors.toUnmodifiableSet());
    }
}
