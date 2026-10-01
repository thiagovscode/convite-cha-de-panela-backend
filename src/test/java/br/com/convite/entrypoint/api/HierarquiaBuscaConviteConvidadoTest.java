package br.com.convite.entrypoint.api;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.entrypoint.api.model.ConvitePublicoResponse;
import br.com.convite.entrypoint.api.model.MembroPublicoResponse;
import br.com.convite.exception.ConvidadoNaoEncontradoException;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.AdicionarMembroFornecedorUseCase;
import br.com.convite.usecase.BuscarConvitePorCodigoUseCase;
import br.com.convite.usecase.BuscarFornecedorPorIdUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HierarquiaBuscaConviteConvidadoTest {

    @Mock
    private BuscarConvitePorCodigoUseCase buscarConvitePorCodigoUseCase;

    @Mock
    private BuscarFornecedorPorIdUseCase buscarFornecedorPorIdUseCase;

    @Mock
    private AdicionarMembroFornecedorUseCase adicionarMembroFornecedorUseCase;

    @Mock
    private ConviteGateway conviteGateway;

    private ConvitePublicoController controller;

    private Convite conviteA;
    private Convite conviteB;

    private static final java.util.UUID MEMBRO_ID_A = java.util.UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final java.util.UUID MEMBRO_ID_B = java.util.UUID.fromString("22222222-2222-2222-2222-222222222222");

    @BeforeEach
    void setUp() {
        controller = new ConvitePublicoController(
                buscarConvitePorCodigoUseCase,
                buscarFornecedorPorIdUseCase,
                adicionarMembroFornecedorUseCase,
                conviteGateway
        );

        conviteA = Convite.builder()
                .id("convite-doc-A")
                .codigo("tn-pwcsuf")
                .familia("Madalena & Família")
                .telefone("11999990001")
                .status("PENDENTE")
                .membros(List.of(
                        MembroConvite.builder()
                                .id(MEMBRO_ID_A)
                                .nome("Madalena Mouraria")
                                .criancaAte6Anos(false)
                                .papel("Convidado")
                                .build()
                ))
                .build();

        conviteB = Convite.builder()
                .id("convite-doc-B")
                .codigo("tn-outro")
                .familia("Família Silva")
                .telefone("11999990002")
                .status("PENDENTE")
                .membros(List.of(
                        MembroConvite.builder()
                                .id(MEMBRO_ID_B)
                                .nome("Roberto Silva")
                                .criancaAte6Anos(false)
                                .papel("Convidado")
                                .build()
                ))
                .build();
    }

    @Test
    @DisplayName("CASO 1 — Buscar somente o convite: por código, sem buscar membros")
    void caso1_buscarSomenteConvite() {
        when(conviteGateway.buscarPorCodigoOuId("tn-pwcsuf")).thenReturn(Optional.of(conviteA));

        ResponseEntity<?> response = controller.buscarConvite("tn-pwcsuf", null, null);

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() instanceof ConvitePublicoResponse);
        ConvitePublicoResponse body = (ConvitePublicoResponse) response.getBody();
        assertEquals("tn-pwcsuf", body.getCodigo());
        assertEquals("Madalena & Família", body.getFamilia());

        // Se convite não encontrado -> 404
        when(conviteGateway.buscarPorCodigoOuId("invalido")).thenReturn(Optional.empty());
        assertThrows(ConviteNaoEncontradoException.class, () -> controller.buscarConvite("invalido", null, null));
    }

    @Test
    @DisplayName("CASO 2 — Buscar convite + convidado válido: Nível 1 código convite, Nível 2 ID membro")
    void caso2_buscarConviteEConvidadoValido() {
        when(conviteGateway.buscarPorCodigo("tn-pwcsuf")).thenReturn(Optional.of(conviteA));

        ResponseEntity<?> response = controller.buscarConvidadoPorConviteEId("tn-pwcsuf", MEMBRO_ID_A.toString());

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertNotNull(body);
        assertEquals("tn-pwcsuf", body.get("codigoConvite"));
        assertEquals("Madalena & Família", body.get("familia"));

        MembroPublicoResponse membro = (MembroPublicoResponse) body.get("convidado");
        assertNotNull(membro);
        assertEquals(MEMBRO_ID_A.toString(), membro.getId());
        assertEquals("Madalena Mouraria", membro.getNome());
    }

    @Test
    @DisplayName("CASO 3 — Convite existe, mas convidado (999) não pertence a ele: erro Convidado não pertence")
    void caso3_conviteExisteMasConvidadoNaoPertence() {
        when(conviteGateway.buscarPorCodigo("tn-pwcsuf")).thenReturn(Optional.of(conviteA));

        ConvidadoNaoEncontradoException ex = assertThrows(
                ConvidadoNaoEncontradoException.class,
                () -> controller.buscarConvidadoPorConviteEId("tn-pwcsuf", "999")
        );

        assertTrue(ex.getMessage().contains("Convidado não pertence a este convite"));
    }

    @Test
    @DisplayName("CASO 4 — Convidado existe em outro convite (conviteB tem membro 2), mas NÃO em conviteA: erro")
    void caso4_convidadoExisteEmOutroConviteMasNaoNoInformado() {
        when(conviteGateway.buscarPorCodigo("tn-pwcsuf")).thenReturn(Optional.of(conviteA));

        // Busca conviteA ("tn-pwcsuf") + membro "2" (que pertence ao conviteB)
        ConvidadoNaoEncontradoException ex = assertThrows(
                ConvidadoNaoEncontradoException.class,
                () -> controller.buscarConvidadoPorConviteEId("tn-pwcsuf", MEMBRO_ID_B.toString())
        );

        assertTrue(ex.getMessage().contains("Convidado não pertence a este convite"));
        // Garante que o conviteB nunca foi consultado
        verify(conviteGateway, never()).buscarPorCodigo("tn-outro");
    }

    @Test
    @DisplayName("CASO 5 — Convite não existe: lanca 404 Convite Não Encontrado e nem tenta buscar membro")
    void caso5_conviteNaoExiste() {
        when(conviteGateway.buscarPorCodigo("invalido")).thenReturn(Optional.empty());
        when(conviteGateway.buscarPorCodigoOuId("invalido")).thenReturn(Optional.empty());

        ConviteNaoEncontradoException ex = assertThrows(
                ConviteNaoEncontradoException.class,
                () -> controller.buscarConvidadoPorConviteEId("invalido", "1")
        );

        assertTrue(ex.getMessage().contains("invalido"));
    }
}
