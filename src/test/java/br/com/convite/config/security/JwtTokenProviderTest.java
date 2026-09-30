package br.com.convite.config.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.SecureRandom;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private String gerarChaveBase64(int bytesQtd) {
        byte[] bytes = new byte[bytesQtd];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    @DisplayName("Deve falhar com IllegalStateException quando o segredo for nulo ou vazio")
    void deveFalharQuandoSegredoEstiverVazio() {
        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "configuredSecret", "");

        IllegalStateException ex = assertThrows(IllegalStateException.class, provider::init);
        assertTrue(ex.getMessage().contains("FALHA CRÍTICA DE CONFIGURAÇÃO"));
    }

    @Test
    @DisplayName("Deve falhar com IllegalStateException quando a chave tiver menos de 64 bytes para HS512")
    void deveFalharQuandoChaveForMenorQue64Bytes() {
        JwtTokenProvider provider = new JwtTokenProvider();
        // 32 bytes em Base64 (apenas 256 bits, insuficiente para HS512)
        String chaveCurta = gerarChaveBase64(32);
        ReflectionTestUtils.setField(provider, "configuredSecret", chaveCurta);

        IllegalStateException ex = assertThrows(IllegalStateException.class, provider::init);
        assertTrue(ex.getMessage().contains("FALHA CRÍTICA DE SEGURANÇA"));
    }

    @Test
    @DisplayName("Deve inicializar com sucesso com chave Base64 de 64 bytes e gerar/validar tokens")
    void deveInicializarComChaveBase64Valida() {
        JwtTokenProvider provider = new JwtTokenProvider();
        String chaveValida = gerarChaveBase64(64);
        ReflectionTestUtils.setField(provider, "configuredSecret", chaveValida);
        ReflectionTestUtils.setField(provider, "customExpirationMs", 3600000L);

        assertDoesNotThrow(provider::init);

        String token = provider.generateTokenForUsername("admin", "ROLE_ADMIN");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("admin", provider.getUsernameFromToken(token));
        assertEquals("ROLE_ADMIN", provider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Deve inicializar com sucesso com chave UTF-8 pura de 64 caracteres ou mais")
    void deveInicializarComChaveUtf8Valida() {
        JwtTokenProvider provider = new JwtTokenProvider();
        // String ASCII com 64 caracteres (64 bytes em UTF-8)
        String chaveUtf8 = "1234567890123456789012345678901234567890123456789012345678901234";
        ReflectionTestUtils.setField(provider, "configuredSecret", chaveUtf8);
        ReflectionTestUtils.setField(provider, "customExpirationMs", 3600000L);

        assertDoesNotThrow(provider::init);

        String token = provider.generateTokenForUsername("recepcao", "ROLE_RECEPCAO");
        assertNotNull(token);
        assertTrue(provider.validateToken(token));
        assertEquals("recepcao", provider.getUsernameFromToken(token));
        assertEquals("ROLE_RECEPCAO", provider.getRoleFromToken(token));
    }

    @Test
    @DisplayName("Deve definir validade padrão do token de exatamente 2 horas (7200000 ms)")
    void deveTerValidadePadraoDeDuasHoras() {
        JwtTokenProvider provider = new JwtTokenProvider();
        assertEquals(7200000L, provider.getExpirationDuration());
        assertEquals(JwtTokenProvider.EXPIRACAO_2_HORAS_MS, provider.getExpirationDuration());
    }
}
