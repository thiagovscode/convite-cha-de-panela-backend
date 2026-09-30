package br.com.convite.usecase;

import br.com.convite.config.security.JwtTokenProvider;
import br.com.convite.config.security.TokenHashUtil;
import br.com.convite.domain.Usuario;
import br.com.convite.entrypoint.api.model.LoginResponse;
import br.com.convite.exception.AutenticacaoInvalidaException;
import br.com.convite.gateway.RefreshTokenGateway;
import br.com.convite.gateway.UsuarioGateway;
import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;
import br.com.convite.usecase.impl.AutenticarUsuarioUseCaseImpl;
import br.com.convite.usecase.impl.RenovarSessaoUseCaseImpl;
import br.com.convite.usecase.impl.RevogarSessaoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = org.mockito.quality.Strictness.LENIENT)
class AutenticacaoRefreshTokenTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RefreshTokenGateway refreshTokenGateway;

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    private JwtTokenProvider jwtTokenProvider;
    private AutenticarUsuarioUseCaseImpl autenticarUseCase;
    private RenovarSessaoUseCaseImpl renovarUseCase;
    private RevogarSessaoUseCaseImpl revogarUseCase;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        byte[] chaveBytes = new byte[64];
        new SecureRandom().nextBytes(chaveBytes);
        String chaveBase64 = Base64.getEncoder().encodeToString(chaveBytes);

        ReflectionTestUtils.setField(jwtTokenProvider, "configuredSecret", chaveBase64);
        ReflectionTestUtils.setField(jwtTokenProvider, "configuredAccessTokenExpirationMs", 900000L); // 15m
        ReflectionTestUtils.setField(jwtTokenProvider, "configuredRefreshTokenExpirationMs", 604800000L); // 7d
        jwtTokenProvider.init();

        autenticarUseCase = new AutenticarUsuarioUseCaseImpl(
                authenticationManager, jwtTokenProvider, refreshTokenGateway, usuarioGateway, passwordEncoder);

        renovarUseCase = new RenovarSessaoUseCaseImpl(jwtTokenProvider, refreshTokenGateway, usuarioGateway);

        revogarUseCase = new RevogarSessaoUseCaseImpl(refreshTokenGateway);
    }

    @Test
    @DisplayName("Login com sucesso deve gerar Access Token JWT e Refresh Token persistido com hash SHA-256")
    void loginComSucessoDeveGerarTokens() {
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("admin");
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))).when(auth).getAuthorities();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(auth);
        lenient().when(usuarioGateway.buscarPorUsername("admin")).thenReturn(Optional.of(
                Usuario.builder().username("admin").password("$2a$10$hash").role("ROLE_ADMIN").build()
        ));

        LoginResponse response = autenticarUseCase.autenticarCompleto("admin", "senha123", "Chrome/Windows");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals(response.getAccessToken(), response.getToken()); // compatibilidade
        assertEquals("Bearer", response.getTokenType());
        assertEquals(900L, response.getExpiresIn());
        assertEquals("admin", response.getUsername());
        assertEquals("ROLE_ADMIN", response.getRole());

        // Valida claims do access token
        assertTrue(jwtTokenProvider.validateToken(response.getAccessToken()));
        assertEquals("admin", jwtTokenProvider.getUsernameFromToken(response.getAccessToken()));
        assertEquals("access", jwtTokenProvider.getTokenTypeFromToken(response.getAccessToken()));

        // Valida que o refresh token foi salvo no gateway como hash
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenGateway).salvar(captor.capture());
        RefreshTokenEntity entitySalva = captor.getValue();

        assertEquals("admin", entitySalva.getUsername());
        assertNotNull(entitySalva.getFamilyId());
        assertFalse(entitySalva.isRevoked());
        // O hash salvo deve ser exatamente o SHA-256 do token opaco
        assertEquals(TokenHashUtil.calcularSha256(response.getRefreshToken()), entitySalva.getTokenHash());
    }

    @Test
    @DisplayName("Login com credenciais inválidas deve lançar AutenticacaoInvalidaException")
    void loginComCredenciaisInvalidasDeveFalhar() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Senha incorreta"));

        assertThrows(AutenticacaoInvalidaException.class, () ->
                autenticarUseCase.autenticarCompleto("admin", "senhaErrada", null));

        verify(refreshTokenGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Renovação com rotação deve invalidar token anterior e gerar novos tokens na mesma família")
    void renovacaoComRotacaoDeveSubstituirToken() {
        String rawTokenAntigo = TokenHashUtil.gerarTokenOpaco();
        String hashAntigo = TokenHashUtil.calcularSha256(rawTokenAntigo);

        RefreshTokenEntity tokenAtual = RefreshTokenEntity.builder()
                .id("token-1")
                .username("admin")
                .tokenHash(hashAntigo)
                .familyId("familia-123")
                .createdAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
                .revoked(false)
                .replacedByTokenHash(null)
                .build();

        when(refreshTokenGateway.buscarPorHash(hashAntigo)).thenReturn(Optional.of(tokenAtual));
        when(usuarioGateway.buscarPorUsername("admin")).thenReturn(Optional.of(
                Usuario.builder().username("admin").role("ROLE_ADMIN").build()
        ));

        LoginResponse response = renovarUseCase.executar(rawTokenAntigo, "Firefox/Linux");

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertNotEquals(rawTokenAntigo, response.getRefreshToken());

        // Token antigo deve ser revogado e referenciar o novo hash
        assertTrue(tokenAtual.isRevoked());
        assertNotNull(tokenAtual.getReplacedByTokenHash());

        // Novo token deve ser persistido na mesma família
        ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
        verify(refreshTokenGateway, atLeast(2)).salvar(captor.capture());

        List<RefreshTokenEntity> salvos = captor.getAllValues();
        RefreshTokenEntity novoSalvo = salvos.get(salvos.size() - 1);
        assertEquals("familia-123", novoSalvo.getFamilyId());
        assertEquals("admin", novoSalvo.getUsername());
        assertFalse(novoSalvo.isRevoked());
        assertEquals(TokenHashUtil.calcularSha256(response.getRefreshToken()), novoSalvo.getTokenHash());
    }

    @Test
    @DisplayName("Detecção de reutilização: tentar usar token já substituído deve revogar toda a família de sessões")
    void deteccaoDeReutilizacaoDeveRevogarFamilia() {
        String rawToken = TokenHashUtil.gerarTokenOpaco();
        String hash = TokenHashUtil.calcularSha256(rawToken);

        // Token que já foi rotacionado e substituído anteriormente
        RefreshTokenEntity tokenComprometido = RefreshTokenEntity.builder()
                .id("token-antigo")
                .username("admin")
                .tokenHash(hash)
                .familyId("familia-suspeita")
                .revoked(true)
                .replacedByTokenHash("hash-mais-novo")
                .expiresAt(Instant.now().plus(6, ChronoUnit.DAYS))
                .build();

        when(refreshTokenGateway.buscarPorHash(hash)).thenReturn(Optional.of(tokenComprometido));

        AutenticacaoInvalidaException ex = assertThrows(AutenticacaoInvalidaException.class, () ->
                renovarUseCase.executar(rawToken, null));

        assertTrue(ex.getMessage().contains("reutilização"));
        // Confirma que toda a família de tokens foi revogada imediatamente
        verify(refreshTokenGateway).revogarFamilia("familia-suspeita");
    }

    @Test
    @DisplayName("Renovação com token expirado deve falhar e marcar token como revogado")
    void renovacaoComTokenExpiradoDeveFalhar() {
        String rawToken = TokenHashUtil.gerarTokenOpaco();
        String hash = TokenHashUtil.calcularSha256(rawToken);

        RefreshTokenEntity tokenExpirado = RefreshTokenEntity.builder()
                .id("token-exp")
                .username("admin")
                .tokenHash(hash)
                .familyId("familia-exp")
                .revoked(false)
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS)) // expirado ontem
                .build();

        when(refreshTokenGateway.buscarPorHash(hash)).thenReturn(Optional.of(tokenExpirado));

        AutenticacaoInvalidaException ex = assertThrows(AutenticacaoInvalidaException.class, () ->
                renovarUseCase.executar(rawToken, null));

        assertTrue(ex.getMessage().contains("expirado"));
        assertTrue(tokenExpirado.isRevoked());
        verify(refreshTokenGateway).salvar(tokenExpirado);
    }

    @Test
    @DisplayName("Logout por token deve revogar o Refresh Token correspondente")
    void logoutDeveRevogarToken() {
        String rawToken = TokenHashUtil.gerarTokenOpaco();
        String hash = TokenHashUtil.calcularSha256(rawToken);

        RefreshTokenEntity tokenAtivo = RefreshTokenEntity.builder()
                .id("token-logout")
                .username("admin")
                .tokenHash(hash)
                .revoked(false)
                .build();

        when(refreshTokenGateway.buscarPorHash(hash)).thenReturn(Optional.of(tokenAtivo));

        revogarUseCase.revogarPorRefreshToken(rawToken);

        assertTrue(tokenAtivo.isRevoked());
        assertNotNull(tokenAtivo.getRevokedAt());
        verify(refreshTokenGateway).salvar(tokenAtivo);
    }

    @Test
    @DisplayName("Logout-All deve revogar todas as sessões do usuário")
    void logoutAllDeveRevogarTodasAsSessoes() {
        revogarUseCase.revogarTodasDoUsuario("admin");
        verify(refreshTokenGateway).revogarTodosPorUsername("admin");
    }
}
