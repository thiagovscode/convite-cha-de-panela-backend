package br.com.convite.usecase;

import br.com.convite.domain.AcompanhanteCasamento;
import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.RsvpCasamento;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.impl.ProcessarConfirmacaoRsvpCasamentoUseCaseImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessarConfirmacaoRsvpCasamentoUseCaseTest {

    @Mock
    private ConviteGateway conviteGateway;

    @Mock
    private ParticipanteCerimoniaGateway participanteCerimoniaGateway;

    @Mock
    private FornecedorGateway fornecedorGateway;

    @Mock
    private ConfirmarRsvpCasamentoUseCase confirmarRsvpCasamentoUseCase;

    private ProcessarConfirmacaoRsvpCasamentoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
                conviteGateway,
                participanteCerimoniaGateway,
                fornecedorGateway,
                confirmarRsvpCasamentoUseCase
        );
    }

    @Test
    @DisplayName("Deve processar confirmação de RSVP com sucesso e atualizar convite")
    void deveProcessarConfirmacaoComSucesso() {
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder().id("m1").nome("Lucas Santos").build());
        membros.add(MembroConvite.builder().id("m2").nome("Mariana Santos").criancaAte6Anos(false).build());

        Convite convite = Convite.builder()
                .id("c1")
                .codigo("LUCAS")
                .familia("Lucas e Mariana")
                .telefone("11988887777")
                .status("PENDENTE")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("LUCAS")).thenReturn(Optional.of(convite));
        when(confirmarRsvpCasamentoUseCase.executar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(participanteCerimoniaGateway.listarTodos()).thenReturn(List.of());
        when(fornecedorGateway.listarTodos()).thenReturn(List.of());

        var acomp = List.of(
                AcompanhanteCasamento.builder().nome("Mariana Santos").criancaAte6Anos(false).build()
        );

        var resultado = useCase.executar(
                "LUCAS",
                "Lucas Santos",
                "11988887777",
                "lucas@email.com",
                true,
                acomp,
                "Parabéns aos noivos!"
        );

        assertTrue(resultado.presenca());
        assertEquals("Presenca confirmada com sucesso!", resultado.message());
        assertEquals(2, resultado.totalPessoas());
        assertEquals(2, resultado.adultos());
        assertEquals(0, resultado.criancasAte6Anos());

        assertEquals("CONFIRMADO", convite.getStatus());
        assertTrue(convite.getMembros().get(0).getConfirmadoRsvp());
        assertTrue(convite.getMembros().get(1).getConfirmadoRsvp());
        verify(conviteGateway, times(1)).salvar(convite);
    }

    @Test
    @DisplayName("Deve lançar exceção quando código de convite não for encontrado")
    void deveFalharQuandoCodigoNaoEncontrado() {
        when(conviteGateway.buscarPorCodigo("INEXISTENTE")).thenReturn(Optional.empty());

        assertThrows(br.com.convite.exception.ConviteNaoEncontradoException.class, () ->
                useCase.executar("INEXISTENTE", "Nome", "11999999999", null, true, null, null)
        );
    }

    @Test
    @DisplayName("Deve barrar tentativa de confirmação com telefone diferente quando já confirmado")
    void deveBloquearConflitoDeTelefoneEmConviteConfirmado() {
        Convite convite = Convite.builder()
                .codigo("JA_CONFIRMADO")
                .status("CONFIRMADO")
                .telefone("11988887777")
                .build();

        when(conviteGateway.buscarPorCodigo("JA_CONFIRMADO")).thenReturn(Optional.of(convite));

        assertThrows(br.com.convite.exception.ConfirmacaoDuplicadaException.class, () ->
                useCase.executar("JA_CONFIRMADO", "Nome", "11911112222", null, true, null, null)
        );
    }

    @Test
    @DisplayName("Deve atualizar flag de criancaAte6Anos no membro do convite quando assinalada no RSVP")
    void deveAtualizarFlagCriancaNoMembroQuandoInformadaNoRsvp() {
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder().id("m1").nome("Carlos Silva").criancaAte6Anos(false).build());
        membros.add(MembroConvite.builder().id("m2").nome("Enzo Silva").criancaAte6Anos(false).build());

        Convite convite = Convite.builder()
                .id("c2")
                .codigo("CARLOS")
                .familia("Família Silva")
                .telefone("11999998888")
                .status("PENDENTE")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("CARLOS")).thenReturn(Optional.of(convite));
        when(confirmarRsvpCasamentoUseCase.executar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(participanteCerimoniaGateway.listarTodos()).thenReturn(List.of());
        when(fornecedorGateway.listarTodos()).thenReturn(List.of());

        var acomp = List.of(
                AcompanhanteCasamento.builder().nome("Enzo Silva").criancaAte6Anos(true).build()
        );

        var resultado = useCase.executar("CARLOS", "Carlos Silva", "11999998888", null, true, acomp, null);

        assertTrue(resultado.presenca());
        assertEquals(2, resultado.totalPessoas());
        assertEquals(1, resultado.adultos());
        assertEquals(1, resultado.criancasAte6Anos());
        assertTrue(convite.getMembros().get(1).getCriancaAte6Anos(), "Membro Enzo Silva deve ser marcado como crianca ate 6 anos");
        verify(conviteGateway, times(1)).salvar(convite);
    }

    @Test
    @DisplayName("Deve distinguir corretamente membros homônimos com o mesmo nome através do ID")
    void deveDistinguirCorretamenteMembrosHomonimosComMesmoNomePorId() {
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder().id("id_pai").nome("Gabriel Oliveira").criancaAte6Anos(false).build());
        membros.add(MembroConvite.builder().id("id_filho").nome("Gabriel Oliveira").criancaAte6Anos(false).build());

        Convite convite = Convite.builder()
                .id("c3")
                .codigo("GABRIEL")
                .familia("Família Oliveira")
                .telefone("11999991111")
                .status("PENDENTE")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("GABRIEL")).thenReturn(Optional.of(convite));
        when(confirmarRsvpCasamentoUseCase.executar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(participanteCerimoniaGateway.listarTodos()).thenReturn(List.of());
        when(fornecedorGateway.listarTodos()).thenReturn(List.of());

        // Filho é confirmado como criança de até 6 anos pelo ID
        var acomp = List.of(
                AcompanhanteCasamento.builder().id("id_filho").nome("Gabriel Oliveira").criancaAte6Anos(true).build()
        );

        var resultado = useCase.executar("GABRIEL", "Gabriel Oliveira", "11999991111", null, true, acomp, null);

        assertTrue(resultado.presenca());
        assertEquals(2, resultado.totalPessoas());
        assertEquals(1, resultado.adultos());
        assertEquals(1, resultado.criancasAte6Anos());

        // O pai não é criança
        assertFalse(convite.getMembros().get(0).getCriancaAte6Anos());
        // O filho é criança
        assertTrue(convite.getMembros().get(1).getCriancaAte6Anos());
        assertTrue(convite.getMembros().get(0).getConfirmadoRsvp());
        assertTrue(convite.getMembros().get(1).getConfirmadoRsvp());
    }

    @Test
    @DisplayName("Deve confirmar presenca normalmente mesmo que o membro titular nao compareca")
    void deveConfirmarPresencaMesmoQueTitularNaoCompareca() {
        List<MembroConvite> membros = new ArrayList<>();
        membros.add(MembroConvite.builder().id("m1").nome("Carlos Silva").build());
        membros.add(MembroConvite.builder().id("m2").nome("Ana Paula Silva").criancaAte6Anos(false).build());

        Convite convite = Convite.builder()
                .id("c4")
                .codigo("CARLOS")
                .familia("Família Silva")
                .telefone("11988889999")
                .status("PENDENTE")
                .membros(membros)
                .build();

        when(conviteGateway.buscarPorCodigo("CARLOS")).thenReturn(Optional.of(convite));
        when(confirmarRsvpCasamentoUseCase.executar(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(participanteCerimoniaGateway.listarTodos()).thenReturn(List.of());
        when(fornecedorGateway.listarTodos()).thenReturn(List.of());

        // Apenas Ana Paula (não titular) confirma presença
        var resultado = useCase.executar(
                "CARLOS",
                "Ana Paula Silva",
                "11988889999",
                "anapaula@email.com",
                true,
                List.of(),
                "Carlos não poderá ir, mas eu estarei lá!"
        );

        assertTrue(resultado.presenca());
        assertEquals(1, resultado.totalPessoas());
        assertEquals(1, resultado.adultos());
        assertEquals(0, resultado.criancasAte6Anos());

        assertEquals("CONFIRMADO", convite.getStatus());
        // Carlos não vai
        assertFalse(convite.getMembros().get(0).getConfirmadoRsvp());
        // Ana Paula vai
        assertTrue(convite.getMembros().get(1).getConfirmadoRsvp());
        verify(conviteGateway, times(1)).salvar(convite);
    }

    @Test
    @DisplayName("Deve lancar excecao quando tentativa de confirmacao ocorrer apos o prazo limite de 23/12/2026")
    void deveLancarExcecaoQuandoDataEstiverAposPrazoLimite() {
        // 2026-12-24T00:00:01 em SP (UTC-3 -> 2026-12-24T03:00:01Z)
        Clock clockExpirado = Clock.fixed(
                Instant.parse("2026-12-24T03:00:01Z"),
                ZoneId.of("America/Sao_Paulo")
        );

        var useCaseExpirado = new ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
                conviteGateway,
                participanteCerimoniaGateway,
                fornecedorGateway,
                confirmarRsvpCasamentoUseCase,
                clockExpirado
        );

        RegraDeNegocioException ex = assertThrows(RegraDeNegocioException.class, () ->
                useCaseExpirado.executar(
                        "QUALQUER",
                        "Nome Teste",
                        "11988887777",
                        null,
                        true,
                        List.of(),
                        null
                )
        );

        assertTrue(ex.getMessage().contains("23/12/2026"));
        assertTrue(ex.getMessage().contains("encerrou"));
    }

    @Test
    @DisplayName("Deve permitir confirmacao de presenca antes de expirar o prazo em 23/12/2026")
    void devePermitirConfirmacaoNoDiaLimiteAntesDoPrazo() {
        // 2026-12-23T23:59:00 em SP (UTC-3 -> 2026-12-24T02:59:00Z)
        Clock clockValido = Clock.fixed(
                Instant.parse("2026-12-24T02:59:00Z"),
                ZoneId.of("America/Sao_Paulo")
        );

        var useCaseValido = new ProcessarConfirmacaoRsvpCasamentoUseCaseImpl(
                conviteGateway,
                participanteCerimoniaGateway,
                fornecedorGateway,
                confirmarRsvpCasamentoUseCase,
                clockValido
        );

        Convite convite = Convite.builder()
                .id("c_prazo")
                .codigo("PRAZO")
                .familia("Família Teste")
                .status("PENDENTE")
                .membros(List.of(MembroConvite.builder().id("m1").nome("Lucas").build()))
                .build();

        when(conviteGateway.buscarPorCodigo("PRAZO")).thenReturn(Optional.of(convite));
        when(confirmarRsvpCasamentoUseCase.executar(any())).thenAnswer(inv -> inv.getArgument(0));
        when(participanteCerimoniaGateway.listarTodos()).thenReturn(List.of());
        when(fornecedorGateway.listarTodos()).thenReturn(List.of());

        var resultado = useCaseValido.executar(
                "PRAZO",
                "Lucas",
                "11999998888",
                null,
                true,
                List.of(),
                null
        );

        assertTrue(resultado.presenca());
    }
}
