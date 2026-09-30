package br.com.convite.usecase.impl;

import br.com.convite.config.security.JwtTokenProvider;
import br.com.convite.exception.AutenticacaoInvalidaException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.UsuarioGateway;
import br.com.convite.usecase.AutenticarUsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UsuarioGateway usuarioGateway;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String executar(String username, String password) {
        String cleanUser = username != null ? username.trim() : "";
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cleanUser, password)
            );

            usuarioGateway.buscarPorUsername(cleanUser).ifPresent(u -> {
                if (u.getPassword() != null && !u.getPassword().startsWith("$2a$") && !u.getPassword().startsWith("$2b$") && !u.getPassword().startsWith("$2y$")) {
                    u.setPassword(passwordEncoder.encode(password));
                    usuarioGateway.salvar(u);
                }
            });

            return jwtTokenProvider.generateToken(authentication);
        } catch (AuthenticationException e) {
            throw new AutenticacaoInvalidaException();
        }
    }
}
