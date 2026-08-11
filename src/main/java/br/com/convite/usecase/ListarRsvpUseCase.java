package br.com.convite.usecase;

import br.com.convite.domain.Rsvp;
import java.util.List;

public interface ListarRsvpUseCase {
    List<Rsvp> executar();
}
