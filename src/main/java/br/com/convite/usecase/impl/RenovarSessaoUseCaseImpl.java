package br.com.convite.usecase.impl;

import br.com.convite.config.security.JwtTokenProvider;
import br.com.convite.config.security.TokenHashUtil;
import br.com.convite.domain.Usuario;
import br.com.convite.entrypoint.api.model.LoginResponse;
import br.com.convite.exception.AutenticacaoInvalidaException;
import br.com.convite.gateway.RefreshTokenGateway;
import br.com.convite.gateway.UsuarioGateway;
import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;
import br.com.convite.usecase.RenovarSessaoUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RenovarSessaoUseCaseImpl implements RenovarSessaoUseCase {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenGateway refreshTokenGateway;
    private final UsuarioGateway usuarioGateway;

    @Override
    public LoginResponse executar(String rawRefreshToken, String deviceInfo) {
        if (rawRefreshToken == null || rawRefreshToken.trim().isBlank()) {
            throw new AutenticacaoInvalidaException("Refresh token é obrigatório.");
        }

        String tokenHash = TokenHashUtil.calcularSha256(rawRefreshToken.trim());
        Optional<RefreshTokenEntity> tokenOpt = refreshTokenGateway.buscarPorHash(tokenHash);

        if (tokenOpt.isEmpty()) {
            log.warn("Tentativa de renovação com refresh token inexistente no banco.");
            throw new AutenticacaoInvalidaException("Refresh token inválido ou não encontrado.");
        }

        RefreshTokenEntity tokenAtual = tokenOpt.get();

        // 1. DETECÇÃO DE REUTILIZAÇÃO (Reuse Attack Detection)
        // Se o token já foi revogado ou já foi substituído por outro token na rotação,
        // significa que um token antigo está sendo reutilizado (potencial roubo de sessão).
        if (tokenAtual.isRevoked() || tokenAtual.getReplacedByTokenHash() != null) {
            log.error("ALERTA DE SEGURANÇA: Tentativa de reutilização de refresh token revogado/substituído! " +
                      "Usuário: '{}', Família: '{}'. Revogando toda a cadeia de tokens da sessão.",
                    tokenAtual.getUsername(), tokenAtual.getFamilyId());

            // Invalida imediatamente toda a família de tokens (todas as sessões vinculadas àquela cadeia)
            refreshTokenGateway.revogarFamilia(tokenAtual.getFamilyId());

            throw new AutenticacaoInvalidaException(
                    "Tentativa de reutilização de sessão revogada detectada. A sessão foi invalidada por segurança.");
        }

        // 2. VERIFICAÇÃO DE EXPIRAÇÃO
        Instant agora = Instant.now();
        if (tokenAtual.getExpiresAt().isBefore(agora)) {
            tokenAtual.setRevoked(true);
            tokenAtual.setRevokedAt(agora);
            refreshTokenGateway.salvar(tokenAtual);
            log.info("Refresh token expirado para o usuário: {}", tokenAtual.getUsername());
            throw new AutenticacaoInvalidaException("Refresh token expirado. Por favor, realize login novamente.");
        }

        // 3. ROTAÇÃO DO REFRESH TOKEN (Token Rotation)
        // O token atual é invalidado e substituído imediatamente por um novo
        tokenAtual.setRevoked(true);
        tokenAtual.setRevokedAt(agora);

        // Gera novo token opaco de alta entropia
        String novoRawRefreshToken = TokenHashUtil.gerarTokenOpaco();
        String novoTokenHash = TokenHashUtil.calcularSha256(novoRawRefreshToken);

        tokenAtual.setReplacedByTokenHash(novoTokenHash);
        refreshTokenGateway.salvar(tokenAtual);

        // Cria o novo Refresh Token na mesma família (mantendo rastreabilidade)
        Instant novaExpiracaoRefresh = agora.plusMillis(jwtTokenProvider.getRefreshTokenExpirationMs());
        RefreshTokenEntity novoTokenEntity = RefreshTokenEntity.builder()
                .username(tokenAtual.getUsername())
                .tokenHash(novoTokenHash)
                .familyId(tokenAtual.getFamilyId()) // Mantém a mesma família
                .createdAt(agora)
                .expiresAt(novaExpiracaoRefresh)
                .revoked(false)
                .deviceInfo(deviceInfo != null ? deviceInfo : tokenAtual.getDeviceInfo())
                .build();

        refreshTokenGateway.salvar(novoTokenEntity);

        // 4. Determina a role atual do usuário no banco
        String username = tokenAtual.getUsername();
        String role = usuarioGateway.buscarPorUsername(username)
                .map(Usuario::getRole)
                .orElse("ROLE_USER");

        if (!role.startsWith("ROLE_")) {
            role = "ROLE_" + role.toUpperCase();
        }

        // 5. Emite novo Access Token JWT
        String novoAccessToken = jwtTokenProvider.generateTokenForUsername(username, role);

        log.info("Sessão renovada com sucesso para o usuário '{}' com rotação de Refresh Token (família: {}).",
                username, tokenAtual.getFamilyId());

        return LoginResponse.builder()
                .token(novoAccessToken) // retrocompatibilidade
                .accessToken(novoAccessToken)
                .refreshToken(novoRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpirationMs() / 1000)
                .username(username)
                .role(role)
                .build();
    }
}
