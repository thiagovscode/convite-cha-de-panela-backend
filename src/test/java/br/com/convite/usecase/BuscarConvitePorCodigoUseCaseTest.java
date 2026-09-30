package br.com.convite.usecase;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.impl.BuscarConvitePorCodigoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BuscarConvitePorCodigoUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    private BuscarConvitePorCodigoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BuscarConvitePorCodigoUseCaseImpl(conviteGateway);
    }

    @Test
    @DisplayName("Deve buscar convite por código existente com sucesso")
    void deveBuscarConvitePorCodigoExistente() {
        Convite convite = Convite.builder()
                .id("conv-1")
                .codigo("FAMILIA123")
                .familia("Família Silva")
                .membros(List.of(
                        MembroConvite.builder().id("m-1").nome("Carlos Silva").build()
                ))
                .build();

        when(conviteGateway.buscarPorCodigo("FAMILIA123")).thenReturn(Optional.of(convite));

        Optional<Convite> resultado = useCase.executar("FAMILIA123");

        assertTrue(resultado.isPresent());
        assertEquals("FAMILIA123", resultado.get().getCodigo());
        assertEquals("Família Silva", resultado.get().getFamilia());
        assertEquals(1, resultado.get().getMembros().size());
        verify(conviteGateway, times(1)).buscarPorCodigo("FAMILIA123");
    }

    @Test
    @DisplayName("Deve retornar Optional vazio quando código for nulo ou em branco")
    void deveRetornarVazioQuandoCodigoInvalido() {
        assertTrue(useCase.executar(null).isEmpty());
        assertTrue(useCase.executar("   ").isEmpty());
        verifyNoInteractions(conviteGateway);
    }
}
