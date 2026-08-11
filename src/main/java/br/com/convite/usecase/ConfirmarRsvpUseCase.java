package br.com.convite.usecase;

import br.com.convite.domain.Rsvp;

public interface ConfirmarRsvpUseCase {
    Rsvp executar(String name, Boolean confirmed, String message);
}
