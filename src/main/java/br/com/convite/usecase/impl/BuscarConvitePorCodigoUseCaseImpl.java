package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.BuscarConvitePorCodigoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BuscarConvitePorCodigoUseCaseImpl implements BuscarConvitePorCodigoUseCase {

    private final ConviteGateway conviteGateway;

    @Override
    public Optional<Convite> executar(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            return Optional.empty();
        }
        return conviteGateway.buscarPorCodigo(codigo.trim())
                .or(() -> conviteGateway.buscarPorCodigoOuId(codigo.trim()));
    }
}
