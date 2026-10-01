package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.ReverterCheckinConvidadoUseCase;
import br.com.convite.usecase.SincronizarPresencaCortejoUseCase;
import br.com.convite.usecase.SincronizarPresencaFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ReverterCheckinConvidadoUseCaseImpl implements ReverterCheckinConvidadoUseCase {

    private final ConviteGateway conviteGateway;
    private final SincronizarPresencaCortejoUseCase sincronizarPresencaCortejoUseCase;
    private final SincronizarPresencaFornecedorUseCase sincronizarPresencaFornecedorUseCase;

    @Override
    public Convite executar(String codigoOuId) {
        if (codigoOuId == null || codigoOuId.isBlank()) {
            throw new RegraDeNegocioException("Código ou ID do convite é obrigatório.");
        }

        Convite convite = conviteGateway.buscarPorCodigoOuId(codigoOuId.trim())
                .orElseThrow(() -> new ConviteNaoEncontradoException(codigoOuId));

        LocalDateTime agora = LocalDateTime.now();

        if (convite.getMembros() != null) {
            for (MembroConvite m : convite.getMembros()) {
                m.setPresenteCheckin(false);
                m.setDataHoraCheckin(null);
                m.setRecepcionista(null);

                // Propaga reversão da presença para cerimônia/cortejo e equipes de fornecedores
                sincronizarPresencaCortejoUseCase.executar(convite.getCodigo(), m, false, agora);
                sincronizarPresencaFornecedorUseCase.executar(m, false, agora);
            }
        }

        convite.setUpdatedAt(agora);
        return conviteGateway.salvar(convite);
    }
}
