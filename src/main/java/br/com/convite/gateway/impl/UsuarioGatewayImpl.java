package br.com.convite.gateway.impl;

import br.com.convite.domain.Usuario;
import br.com.convite.gateway.UsuarioGateway;
import br.com.convite.gateway.persistence.UsuarioRepository;
import br.com.convite.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UsuarioGatewayImpl implements UsuarioGateway {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Optional<Usuario> buscarPorUsername(String username) {
        return usuarioRepository.findByUsername(username)
                .map(usuarioMapper::toDomain);
    }
}
