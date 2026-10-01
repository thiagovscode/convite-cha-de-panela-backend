package br.com.convite.usecase;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.usecase.impl.ResetarRsvpConviteUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ResetarRsvpConviteUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    @Mock
    private RsvpCasamentoGateway rsvpCasamentoGateway;

    @Mock
    private SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    private ResetarRsvpConviteUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ResetarRsvpConviteUseCaseImpl(
                conviteGateway,
                rsvpCasamentoGateway,
                sincronizarCortejoConviteUseCase
        );
    }

    @Test
    @DisplayName("Deve resetar convite pelo código e IDs dos membros, apagando RSVP pelo código do convite")
    void deveResetarConvitePorCodigoEIdsMembros() {
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder()
                .id(UUID.randomUUID())
                .nome("Carlos")
                .confirmadoRsvp(false)
                .presenteCheckin(false)
                .recepcionista("Portaria 1")
                .build());
        membros.add(MembroConvite.builder()
                .id(UUID.randomUUID())
                .nome("Fernanda")
                .confirmadoRsvp(false)
                .build());

        Convite convite = Convite.builder()
                .id("convite-123")
                .codigo("CARLOS-FER")
                .familia("Silva")
                .status("RECUSADO")
                .dataConfirmacao(LocalDateTime.now())
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("CARLOS-FER")).thenReturn(Optional.of(convite));
        when(conviteGateway.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Convite resultado = useCase.executar("CARLOS-FER");

        assertNotNull(resultado);
        assertEquals("PENDENTE", resultado.getStatus());
        assertNull(resultado.getDataConfirmacao());

        // Valida que o RSVP foi deletado pelo código do convite (não por telefone)
        verify(rsvpCasamentoGateway, times(1)).deletarPorCodigoConvite("CARLOS-FER");

        // Valida que cada membro (pelo ID) teve seus dados de RSVP e checkin resetados
        assertEquals(2, resultado.getMembros().size());
        for (MembroConvite m : resultado.getMembros()) {
            assertNull(m.getConfirmadoRsvp(), "Status de RSVP do membro deve ser null após reset");
            assertNull(m.getPresenteCheckin(), "Presença do membro deve ser null após reset");
            assertNull(m.getDataHoraCheckin(), "DataHoraCheckin deve ser null após reset");
            assertNull(m.getRecepcionista(), "Recepcionista deve ser null após reset");
        }

        verify(conviteGateway, times(1)).salvar(convite);
        verify(sincronizarCortejoConviteUseCase, times(1)).executar(any());
    }

    @Test
    @DisplayName("Deve lançar ConviteNaoEncontradoException quando código for inexistente")
    void deveLancarExcecaoQuandoNaoEncontrado() {
        when(conviteGateway.buscarPorCodigo("INEXISTENTE")).thenReturn(Optional.empty());
        when(conviteGateway.buscarPorId("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(ConviteNaoEncontradoException.class, () -> useCase.executar("INEXISTENTE"));
    }
}
