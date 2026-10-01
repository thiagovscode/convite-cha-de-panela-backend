package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.DefinirParCortejoUseCase;
import br.com.convite.usecase.SincronizarCortejoConviteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DefinirParCortejoUseCaseImpl implements DefinirParCortejoUseCase {

    private final ConviteGateway conviteGateway;
    private final SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    @Override
    public Convite executar(String codigoConvite, String membroId, String nomeMembro, String nomePar) {
        // 1. Localiza o convite principal
        Convite convitePrincipal = null;
        if (codigoConvite != null && !codigoConvite.isBlank()) {
            convitePrincipal = conviteGateway.buscarPorCodigo(codigoConvite.trim()).orElse(null);
        }

        if (convitePrincipal == null && nomeMembro != null && !nomeMembro.isBlank()) {
            List<Convite> encontrados = conviteGateway.buscarPorNomeMembro(nomeMembro.trim());
            if (!encontrados.isEmpty()) {
                convitePrincipal = encontrados.get(0);
            } else {
                convitePrincipal = conviteGateway.listarTodos().stream()
                        .filter(c -> c.getMembros() != null && c.getMembros().stream()
                                .anyMatch(m -> m.getNome() != null && m.getNome().equalsIgnoreCase(nomeMembro.trim())))
                        .findFirst()
                        .orElse(null);
            }
        }

        if (convitePrincipal == null) {
            throw new RegraDeNegocioException("Convite não encontrado.");
        }

        // 2. Localiza o membro principal dentro do convite
        MembroConvite membroA = null;
        UUID idMembroUuid = null;
        if (membroId != null && !membroId.isBlank()) {
            try {
                idMembroUuid = UUID.fromString(membroId.trim());
            } catch (IllegalArgumentException ignored) {}
        }

        if (convitePrincipal.getMembros() != null) {
            for (MembroConvite m : convitePrincipal.getMembros()) {
                boolean matchId = (idMembroUuid != null && idMembroUuid.equals(m.getId()))
                        || (membroId != null && m.getId() != null && m.getId().toString().equalsIgnoreCase(membroId.trim()));
                boolean matchNome = (nomeMembro != null && !nomeMembro.isBlank() && m.getNome() != null && m.getNome().trim().equalsIgnoreCase(nomeMembro.trim()));

                if (matchId || matchNome) {
                    membroA = m;
                    break;
                }
            }
        }

        if (membroA == null) {
            throw new RegraDeNegocioException("Membro não encontrado no convite informado.");
        }

        String nomeRealA = membroA.getNome().trim();
        String antigoParA = (membroA.getPar() != null && !membroA.getPar().isBlank()) ? membroA.getPar().trim() : null;
        String novoParB = (nomePar != null && !nomePar.isBlank()) ? nomePar.trim() : null;

        // 3. Se o par anterior mudou ou foi removido, desvincula reciprocamente o par anterior
        if (antigoParA != null && (novoParB == null || !antigoParA.equalsIgnoreCase(novoParB))) {
            desvincularPar(convitePrincipal, antigoParA, nomeRealA);
        }

        // 4. Atualiza o par no membro principal
        membroA.setPar(novoParB);

        // 5. Se foi informado um novo par, vincula reciprocamente
        if (novoParB != null) {
            vincularParReciproco(convitePrincipal, membroA, novoParB, nomeRealA);
        }

        // 6. Salva o convite principal e sincroniza participantes do cortejo
        Convite salvo = conviteGateway.salvar(convitePrincipal);
        sincronizarCortejoConviteUseCase.executar(salvo);
        log.info("Par de cortejo atualizado para o membro '{}': '{}' (Convite: {})", nomeRealA, novoParB, salvo.getCodigo());

        return salvo;
    }

    private void desvincularPar(Convite convitePrincipal, String nomeParADesvincular, String nomeMembroReferencia) {
        // Verifica no próprio convite principal
        if (convitePrincipal.getMembros() != null) {
            for (MembroConvite m : convitePrincipal.getMembros()) {
                if (m.getNome() != null && m.getNome().trim().equalsIgnoreCase(nomeParADesvincular)) {
                    if (m.getPar() != null && m.getPar().trim().equalsIgnoreCase(nomeMembroReferencia)) {
                        m.setPar(null);
                    }
                }
            }
        }

        // Busca em outros convites
        List<Convite> outros = conviteGateway.buscarPorNomeMembro(nomeParADesvincular);
        if (outros.isEmpty()) {
            outros = conviteGateway.listarTodos().stream()
                    .filter(c -> c.getMembros() != null && c.getMembros().stream()
                            .anyMatch(m -> m.getNome() != null && m.getNome().trim().equalsIgnoreCase(nomeParADesvincular)))
                    .toList();
        }

        for (Convite outro : outros) {
            if (outro.getCodigo() != null && outro.getCodigo().equalsIgnoreCase(convitePrincipal.getCodigo())) {
                continue;
            }
            boolean alterou = false;
            if (outro.getMembros() != null) {
                for (MembroConvite m : outro.getMembros()) {
                    if (m.getNome() != null && m.getNome().trim().equalsIgnoreCase(nomeParADesvincular)) {
                        if (m.getPar() != null && m.getPar().trim().equalsIgnoreCase(nomeMembroReferencia)) {
                            m.setPar(null);
                            alterou = true;
                        }
                    }
                }
            }
            if (alterou) {
                conviteGateway.salvar(outro);
                sincronizarCortejoConviteUseCase.executar(outro);
            }
        }
    }

    private void vincularParReciproco(Convite convitePrincipal, MembroConvite membroA, String novoParB, String nomeRealA) {
        // 1. Verifica se o novo par está dentro do MESMO convite
        boolean encontradoNoMesmo = false;
        if (convitePrincipal.getMembros() != null) {
            for (MembroConvite mB : convitePrincipal.getMembros()) {
                if (mB != membroA && mB.getNome() != null && mB.getNome().trim().equalsIgnoreCase(novoParB)) {
                    String antigoParDeB = (mB.getPar() != null && !mB.getPar().isBlank()) ? mB.getPar().trim() : null;
                    if (antigoParDeB != null && !antigoParDeB.equalsIgnoreCase(nomeRealA)) {
                        desvincularPar(convitePrincipal, antigoParDeB, mB.getNome().trim());
                    }
                    mB.setPar(nomeRealA);
                    encontradoNoMesmo = true;
                    break;
                }
            }
        }

        if (encontradoNoMesmo) {
            return;
        }

        // 2. Busca o novo par em outros convites
        List<Convite> outros = conviteGateway.buscarPorNomeMembro(novoParB);
        if (outros.isEmpty()) {
            outros = conviteGateway.listarTodos().stream()
                    .filter(c -> c.getMembros() != null && c.getMembros().stream()
                            .anyMatch(m -> m.getNome() != null && m.getNome().trim().equalsIgnoreCase(novoParB)))
                    .toList();
        }

        for (Convite outro : outros) {
            if (outro.getCodigo() != null && outro.getCodigo().equalsIgnoreCase(convitePrincipal.getCodigo())) {
                continue;
            }
            boolean alterouOutro = false;
            if (outro.getMembros() != null) {
                for (MembroConvite mB : outro.getMembros()) {
                    if (mB.getNome() != null && mB.getNome().trim().equalsIgnoreCase(novoParB)) {
                        String antigoParDeB = (mB.getPar() != null && !mB.getPar().isBlank()) ? mB.getPar().trim() : null;
                        if (antigoParDeB != null && !antigoParDeB.equalsIgnoreCase(nomeRealA)) {
                            desvincularPar(outro, antigoParDeB, mB.getNome().trim());
                        }
                        mB.setPar(nomeRealA);
                        alterouOutro = true;
                    }
                }
            }
            if (alterouOutro) {
                conviteGateway.salvar(outro);
                sincronizarCortejoConviteUseCase.executar(outro);
            }
        }
    }
}
