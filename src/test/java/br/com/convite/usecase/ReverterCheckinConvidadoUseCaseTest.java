package br.com.convite.usecase;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.impl.ReverterCheckinConvidadoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReverterCheckinConvidadoUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    @Mock
    private SincronizarPresencaCortejoUseCase sincronizarPresencaCortejoUseCase;

    @Mock
    private SincronizarPresencaFornecedorUseCase sincronizarPresencaFornecedorUseCase;

    private ReverterCheckinConvidadoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReverterCheckinConvidadoUseCaseImpl(
                conviteGateway,
                sincronizarPresencaCortejoUseCase,
                sincronizarPresencaFornecedorUseCase
        );
    }

    @Test
    @DisplayName("Deve reverter checkin total e propagar presença falsa para cortejo e fornecedores")
    void deveReverterCheckinComSucesso() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder()
                .id(id1)
                .nome("Ana Padrinho")
                .presenteCheckin(true)
                .dataHoraCheckin(LocalDateTime.now())
                .recepcionista("Portaria 1")
                .build());
        membros.add(MembroConvite.builder()
                .id(id2)
                .nome("Beto Fornecedor")
                .presenteCheckin(true)
                .dataHoraCheckin(LocalDateTime.now())
                .recepcionista("Portaria 1")
                .build());

        Convite convite = Convite.builder()
                .id("c1")
                .codigo("ABC12")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigoOuId("ABC12")).thenReturn(Optional.of(convite));
        when(conviteGateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Convite resultado = useCase.executar("ABC12");

        assertNotNull(resultado);
        for (MembroConvite m : resultado.getMembros()) {
            assertFalse(m.getPresenteCheckin(), "Membro deveria ter presença revertida");
            assertNull(m.getDataHoraCheckin(), "Data de checkin deveria ser limpa");
            assertNull(m.getRecepcionista(), "Recepcionista deveria ser limpo");
        }

        // Verifica que sincronização foi chamada com presente = false para todos os membros
        verify(sincronizarPresencaCortejoUseCase, times(2))
                .executar(eq("ABC12"), any(MembroConvite.class), eq(false), any(LocalDateTime.class));
        verify(sincronizarPresencaFornecedorUseCase, times(2))
                .executar(any(MembroConvite.class), eq(false), any(LocalDateTime.class));
        verify(conviteGateway, times(1)).salvar(convite);
    }
}
