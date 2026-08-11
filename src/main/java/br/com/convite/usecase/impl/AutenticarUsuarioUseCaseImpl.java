package br.com.convite.usecase.impl;

import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.usecase.AutenticarUsuarioUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

    private final AuthenticationManager authenticationManager;
    private final br.com.convite.config.security.JwtTokenProvider jwtTokenProvider;

    @Override
    public String executar(String username, String password) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            return jwtTokenProvider.generateToken(authentication);
        } catch (AuthenticationException e) {
            throw new RegraDeNegocioException("Credenciais inválidas");
        }
    }
}
