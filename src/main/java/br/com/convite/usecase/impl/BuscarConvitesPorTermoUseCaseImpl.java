package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.BuscarConvitesPorTermoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BuscarConvitesPorTermoUseCaseImpl implements BuscarConvitesPorTermoUseCase {

    private final ConviteGateway conviteGateway;

    @Override
    public List<Convite> executar(String termo) {
        if (termo == null || termo.isBlank()) {
            return List.of();
        }
        return conviteGateway.buscarPorTermo(termo.trim());
    }
}
