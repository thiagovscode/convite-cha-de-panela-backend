package br.com.convite.entrypoint.api;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminConviteControllerTest {

    @Mock
    private ListarConvitesUseCase listarConvitesUseCase;

    @Mock
    private SalvarConviteUseCase salvarConviteUseCase;

    @Mock
    private CriarConviteUseCase criarConviteUseCase;

    @Mock
    private AtualizarConviteUseCase atualizarConviteUseCase;

    @Mock
    private ExcluirConviteUseCase excluirConviteUseCase;

    @Mock
    private CalcularMetricasCasamentoUseCase calcularMetricasCasamentoUseCase;

    @Mock
    private ConviteGateway conviteGateway;

    @Mock
    private SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    @Mock
    private ResetarRsvpConviteUseCase resetarRsvpConviteUseCase;

    private AdminConviteController controller;

    @BeforeEach
    void setUp() {
        controller = new AdminConviteController(
                listarConvitesUseCase,
                salvarConviteUseCase,
                criarConviteUseCase,
                atualizarConviteUseCase,
                excluirConviteUseCase,
                calcularMetricasCasamentoUseCase,
                conviteGateway,
                sincronizarCortejoConviteUseCase,
                resetarRsvpConviteUseCase
        );
    }

    @Test
    @DisplayName("Deve delegar reset de RSVP para o Use Case e retornar 200 com sucesso")
    void deveDelegarResetParaUseCase() {
        Convite conviteResetado = Convite.builder()
                .codigo("ABC123")
                .familia("Silva")
                .status("PENDENTE")
                .build();

        when(resetarRsvpConviteUseCase.executar("ABC123")).thenReturn(conviteResetado);

        ResponseEntity<?> response = controller.resetarRsvp("ABC123");

        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("PENDENTE", body.get("status"));
        assertEquals("ABC123", body.get("codigo"));

        verify(resetarRsvpConviteUseCase, times(1)).executar("ABC123");
    }
}
