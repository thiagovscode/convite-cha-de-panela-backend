package br.com.convite.usecase;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.MetricasCasamento;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.impl.CalcularMetricasCasamentoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CalcularMetricasCasamentoUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    private CalcularMetricasCasamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CalcularMetricasCasamentoUseCaseImpl(conviteGateway);
    }

    @Test
    @DisplayName("Deve calcular corretamente as métricas de convites e convidados")
    void deveCalcularMetricasComSucesso() {
        Convite c1 = Convite.builder()
                .codigo("FAM1")
                .status("CONFIRMADO")
                .membros(List.of(
                        MembroConvite.builder().nome("Pai").criancaAte6Anos(false).confirmadoRsvp(true).build(),
                        MembroConvite.builder().nome("Filho").criancaAte6Anos(true).confirmadoRsvp(true).build()
                ))
                .build();

        Convite c2 = Convite.builder()
                .codigo("FAM2")
                .status("RECUSADO")
                .membros(List.of(
                        MembroConvite.builder().nome("Amigo").criancaAte6Anos(false).confirmadoRsvp(false).build()
                ))
                .build();

        Convite c3 = Convite.builder()
                .codigo("FAM3")
                .status("PENDENTE")
                .membros(List.of(
                        MembroConvite.builder().nome("Tio").criancaAte6Anos(false).confirmadoRsvp(null).build()
                ))
                .build();

        when(conviteGateway.listarTodos()).thenReturn(List.of(c1, c2, c3));

        MetricasCasamento metricas = useCase.executar();

        assertEquals(3, metricas.getTotalConvites());
        assertEquals(1, metricas.getTotalConvitesConfirmados());
        assertEquals(1, metricas.getTotalConvitesRecusados());
        assertEquals(1, metricas.getTotalConvitesPendentes());

        assertEquals(4, metricas.getTotalPessoas());
        assertEquals(2, metricas.getTotalConfirmados());
        assertEquals(1, metricas.getTotalAdultosConfirmados());
        assertEquals(1, metricas.getTotalCriancasConfirmadas());
        assertEquals(1, metricas.getTotalRecusaram());
        assertEquals(1, metricas.getTotalPendentes());
    }
}
