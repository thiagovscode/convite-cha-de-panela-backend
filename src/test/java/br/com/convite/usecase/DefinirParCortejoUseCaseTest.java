package br.com.convite.usecase;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.impl.DefinirParCortejoUseCaseImpl;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DefinirParCortejoUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    @Mock
    private SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    private DefinirParCortejoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DefinirParCortejoUseCaseImpl(conviteGateway, sincronizarCortejoConviteUseCase);
    }

    @Test
    @DisplayName("Deve vincular reciprocamente dois membros em convites distintos (Misael & Anie)")
    void deveVincularParesReciprocamenteEmConvitesDistintos() {
        UUID idMisael = UUID.randomUUID();
        Convite conviteMisael = Convite.builder()
                .codigo("misael")
                .familia("Misael")
                .membros(new ArrayList<>(List.of(
                        MembroConvite.builder().id(idMisael).nome("Misael").papel("Padrinho").build()
                )))
                .build();

        UUID idAnie = UUID.randomUUID();
        Convite conviteAnie = Convite.builder()
                .codigo("anie")
                .familia("Anie")
                .membros(new ArrayList<>(List.of(
                        MembroConvite.builder().id(idAnie).nome("Anie").papel("Madrinha").build()
                )))
                .build();

        when(conviteGateway.buscarPorCodigo("misael")).thenReturn(Optional.of(conviteMisael));
        when(conviteGateway.buscarPorNomeMembro("Anie")).thenReturn(List.of(conviteAnie));
        when(conviteGateway.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Convite resultado = useCase.executar("misael", idMisael.toString(), "Misael", "Anie");

        assertNotNull(resultado);
        assertEquals("Anie", resultado.getMembros().get(0).getPar());
        assertEquals("Misael", conviteAnie.getMembros().get(0).getPar());

        verify(conviteGateway, times(1)).salvar(conviteAnie);
        verify(conviteGateway, times(1)).salvar(conviteMisael);
        verify(sincronizarCortejoConviteUseCase, times(1)).executar(conviteAnie);
        verify(sincronizarCortejoConviteUseCase, times(1)).executar(conviteMisael);
    }

    @Test
    @DisplayName("Deve vincular reciprocamente dois membros dentro do mesmo convite")
    void deveVincularParesReciprocamenteNoMesmoConvite() {
        UUID idLeonardo = UUID.randomUUID();
        UUID idAmanda = UUID.randomUUID();
        Convite conviteCasal = Convite.builder()
                .codigo("casal-padrinhos")
                .familia("Leonardo & Amanda")
                .membros(new ArrayList<>(List.of(
                        MembroConvite.builder().id(idLeonardo).nome("Leonardo").papel("Padrinho").build(),
                        MembroConvite.builder().id(idAmanda).nome("Amanda").papel("Madrinha").build()
                )))
                .build();

        when(conviteGateway.buscarPorCodigo("casal-padrinhos")).thenReturn(Optional.of(conviteCasal));
        when(conviteGateway.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Convite resultado = useCase.executar("casal-padrinhos", idLeonardo.toString(), "Leonardo", "Amanda");

        assertNotNull(resultado);
        assertEquals("Amanda", resultado.getMembros().get(0).getPar());
        assertEquals("Leonardo", resultado.getMembros().get(1).getPar());

        verify(conviteGateway, times(1)).salvar(conviteCasal);
        verify(sincronizarCortejoConviteUseCase, times(1)).executar(conviteCasal);
    }

    @Test
    @DisplayName("Deve desvincular reciprocamente quando o par for removido (forcar vazio)")
    void deveDesvincularReciprocamenteAoRemoverPar() {
        UUID idMisael = UUID.randomUUID();
        Convite conviteMisael = Convite.builder()
                .codigo("misael")
                .familia("Misael")
                .membros(new ArrayList<>(List.of(
                        MembroConvite.builder().id(idMisael).nome("Misael").papel("Padrinho").par("Anie").build()
                )))
                .build();

        UUID idAnie = UUID.randomUUID();
        Convite conviteAnie = Convite.builder()
                .codigo("anie")
                .familia("Anie")
                .membros(new ArrayList<>(List.of(
                        MembroConvite.builder().id(idAnie).nome("Anie").papel("Madrinha").par("Misael").build()
                )))
                .build();

        when(conviteGateway.buscarPorCodigo("misael")).thenReturn(Optional.of(conviteMisael));
        when(conviteGateway.buscarPorNomeMembro("Anie")).thenReturn(List.of(conviteAnie));
        when(conviteGateway.salvar(any())).thenAnswer(inv -> inv.getArgument(0));

        Convite resultado = useCase.executar("misael", idMisael.toString(), "Misael", "");

        assertNotNull(resultado);
        assertNull(resultado.getMembros().get(0).getPar());
        assertNull(conviteAnie.getMembros().get(0).getPar());

        verify(conviteGateway, times(1)).salvar(conviteAnie);
        verify(conviteGateway, times(1)).salvar(conviteMisael);
    }
}
