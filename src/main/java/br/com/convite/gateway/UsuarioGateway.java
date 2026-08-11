package br.com.convite.gateway;

import br.com.convite.domain.Usuario;
import java.util.Optional;

public interface UsuarioGateway {
    Optional<Usuario> buscarPorUsername(String username);
}
