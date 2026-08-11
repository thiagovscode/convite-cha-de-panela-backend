package br.com.convite.usecase.impl;

import br.com.convite.domain.Rsvp;
import br.com.convite.gateway.RsvpGateway;
import br.com.convite.usecase.ListarRsvpUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListarRsvpUseCaseImpl implements ListarRsvpUseCase {

    private final RsvpGateway rsvpGateway;

    @Override
    public List<Rsvp> executar() {
        return rsvpGateway.listarTodos();
    }
}
