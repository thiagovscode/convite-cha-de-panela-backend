package br.com.convite.usecase;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.CheckinDuplicadoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.impl.RegistrarCheckinConvidadoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrarCheckinConvidadoUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    @Mock
    private SincronizarPresencaCortejoUseCase sincronizarPresencaCortejoUseCase;

    @Mock
    private SincronizarPresencaFornecedorUseCase sincronizarPresencaFornecedorUseCase;

    private RegistrarCheckinConvidadoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegistrarCheckinConvidadoUseCaseImpl(
                conviteGateway,
                sincronizarPresencaCortejoUseCase,
                sincronizarPresencaFornecedorUseCase
        );
    }

    @Test
    @DisplayName("Deve registrar checkin nominal na recepção com sucesso")
    void deveRegistrarCheckinComSucesso() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder().id(id1).nome("Ana").presenteCheckin(false).build());
        membros.add(MembroConvite.builder().id(id2).nome("Beto").presenteCheckin(false).build());

        Convite convite = Convite.builder()
                .id("c1")
                .codigo("ABC12")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("ABC12")).thenReturn(Optional.of(convite));
        when(conviteGateway.salvar(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var comando = new RegistrarCheckinConvidadoUseCase.Comando(
                "ABC12",
                "Portaria 1",
                List.of(
                        new RegistrarCheckinConvidadoUseCase.PresencaMembro(id1.toString(), true),
                        new RegistrarCheckinConvidadoUseCase.PresencaMembro(id2.toString(), false)
                )
        );

        var resultado = useCase.executar(comando);

        assertNotNull(resultado);
        assertTrue(resultado.convite().getMembros().get(0).getPresenteCheckin());
        assertFalse(resultado.convite().getMembros().get(1).getPresenteCheckin());
        assertEquals("Portaria 1", resultado.convite().getMembros().get(0).getRecepcionista());
        verify(conviteGateway, times(1)).salvar(convite);
    }

    @Test
    @DisplayName("Deve impedir duplicidade quando todos já entraram")
    void deveBloquearDuplicidadeQuandoTodosJaEntraram() {
        UUID idJaEntrou = UUID.randomUUID();
        List<MembroConvite> membros = List.of(
                MembroConvite.builder().id(idJaEntrou).nome("Ana").presenteCheckin(true).build()
        );

        Convite convite = Convite.builder()
                .id("c1")
                .codigo("JA_ENTROU")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("JA_ENTROU")).thenReturn(Optional.of(convite));

        var comando = new RegistrarCheckinConvidadoUseCase.Comando(
                "JA_ENTROU",
                "Portaria 1",
                List.of(
                        new RegistrarCheckinConvidadoUseCase.PresencaMembro(idJaEntrou.toString(), true)
                )
        );

        assertThrows(CheckinDuplicadoException.class, () -> useCase.executar(comando));
        verify(conviteGateway, never()).salvar(any());
    }
}
