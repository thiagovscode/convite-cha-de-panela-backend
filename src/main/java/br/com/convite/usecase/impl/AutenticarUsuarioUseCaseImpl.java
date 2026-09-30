package br.com.convite.usecase.impl;

import br.com.convite.config.security.JwtTokenProvider;
import br.com.convite.config.security.TokenHashUtil;
import br.com.convite.domain.Usuario;
import br.com.convite.entrypoint.api.model.LoginResponse;
import br.com.convite.exception.AutenticacaoInvalidaException;
import br.com.convite.gateway.RefreshTokenGateway;
import br.com.convite.gateway.UsuarioGateway;
import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;
import br.com.convite.usecase.AutenticarUsuarioUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutenticarUsuarioUseCaseImpl implements AutenticarUsuarioUseCase {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenGateway refreshTokenGateway;
    private final UsuarioGateway usuarioGateway;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String executar(String username, String password) {
        return autenticarCompleto(username, password, null).getAccessToken();
    }

    @Override
    public LoginResponse autenticarCompleto(String username, String password, String deviceInfo) {
        String cleanUser = username != null ? username.trim() : "";
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(cleanUser, password)
            );

            // Migração transparente de senha em texto puro para BCrypt (se houver legado)
            Optional<Usuario> usuarioOpt = usuarioGateway.buscarPorUsername(cleanUser);
            usuarioOpt.ifPresent(u -> {
                if (u.getPassword() != null && !u.getPassword().startsWith("$2a$")
                        && !u.getPassword().startsWith("$2b$") && !u.getPassword().startsWith("$2y$")) {
                    u.setPassword(passwordEncoder.encode(password));
                    usuarioGateway.salvar(u);
                }
            });

            String role = authentication.getAuthorities().stream()
                    .findFirst()
                    .map(GrantedAuthority::getAuthority)
                    .orElse("ROLE_USER");

            // 1. Gera Access Token (JWT curto com claims sub, iat, exp, jti, token_type=access)
            String accessToken = jwtTokenProvider.generateTokenForUsername(cleanUser, role);

            // 2. Gera Refresh Token opaco criptograficamente seguro (512 bits)
            String rawRefreshToken = TokenHashUtil.gerarTokenOpaco();
            String tokenHash = TokenHashUtil.calcularSha256(rawRefreshToken);
            String familyId = UUID.randomUUID().toString();

            Instant agora = Instant.now();
            Instant expiracaoRefresh = agora.plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());

            // 3. Persiste o Refresh Token no banco com hash SHA-256
            RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                    .username(cleanUser)
                    .tokenHash(tokenHash)
                    .familyId(familyId)
                    .createdAt(agora)
                    .expiresAt(expiracaoRefresh)
                    .revoked(false)
                    .deviceInfo(deviceInfo)
                    .build();

            refreshTokenGateway.salvar(refreshTokenEntity);

            log.info("Usuário '{}' autenticado com sucesso. Access Token e Refresh Token emitidos (família: {}).",
                    cleanUser, familyId);

            return LoginResponse.builder()
                    .token(accessToken) // retrocompatibilidade com frontend legado
                    .accessToken(accessToken)
                    .refreshToken(rawRefreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                    .username(cleanUser)
                    .role(role)
                    .build();

        } catch (AuthenticationException e) {
            log.warn("Tentativa de login inválida para o usuário: {}", cleanUser);
            throw new AutenticacaoInvalidaException("Usuário ou senha inválidos.");
        }
    }
}
