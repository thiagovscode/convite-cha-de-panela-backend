package br.com.convite.gateway;

import br.com.convite.domain.Rsvp;
import java.util.List;

public interface RsvpGateway {
    Rsvp salvar(Rsvp rsvp);
    List<Rsvp> listarTodos();
}
