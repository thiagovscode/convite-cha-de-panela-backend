package br.com.convite.gateway;

import br.com.convite.domain.Presente;
import java.util.List;
import java.util.Optional;

public interface PresenteGateway {
    List<Presente> listarTodos();
    Optional<Presente> buscarPorId(Long id);
    Presente salvar(Presente presente);
}
