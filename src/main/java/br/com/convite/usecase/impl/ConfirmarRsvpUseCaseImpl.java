package br.com.convite.usecase.impl;

import br.com.convite.domain.Rsvp;
import br.com.convite.gateway.RsvpGateway;
import br.com.convite.usecase.ConfirmarRsvpUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ConfirmarRsvpUseCaseImpl implements ConfirmarRsvpUseCase {

    private final RsvpGateway rsvpGateway;

    @Override
    public Rsvp executar(String name, Boolean confirmed, String message) {
        Rsvp rsvp = Rsvp.builder()
                .name(name)
                .confirmed(confirmed)
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        
        return rsvpGateway.salvar(rsvp);
    }
}
