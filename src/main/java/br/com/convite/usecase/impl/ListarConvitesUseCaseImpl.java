package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.ListarConvitesUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarConvitesUseCaseImpl implements ListarConvitesUseCase {

    private final ConviteGateway conviteGateway;

    @Override
    public List<Convite> executar() {
        return conviteGateway.listarTodos();
    }
}
