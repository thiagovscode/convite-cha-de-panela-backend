package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.exception.ParticipanteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.CheckinParticipanteCerimoniaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CheckinParticipanteCerimoniaUseCaseImpl implements CheckinParticipanteCerimoniaUseCase {

    private final ParticipanteCerimoniaGateway participanteGateway;
    private final ConviteGateway conviteGateway;

    @Override
    public ParticipanteCerimonia executar(String id, Boolean presente) {
        return executar(id, presente, null);
    }

    @Override
    public ParticipanteCerimonia executar(String id, Boolean presente, String statusCortejo) {
        if (id == null || id.isBlank()) {
            throw new RegraDeNegocioException("Identificador é obrigatório.");
        }

        ParticipanteCerimonia p = participanteGateway.buscarPorId(id.trim())
                .orElseThrow(() -> new ParticipanteNaoEncontradoException(id));

        boolean novoStatus = (presente != null) ? presente : !Boolean.TRUE.equals(p.getPresenteCheckin());
        p.setPresenteCheckin(novoStatus);
        p.setDataHoraEntrada(novoStatus ? LocalDateTime.now() : null);

        if (statusCortejo != null && !statusCortejo.isBlank()) {
            String statusNorm = statusCortejo.trim().toUpperCase();
            if (!java.util.Set.of("AGUARDANDO_CHEGADA", "NO_LOCAL").contains(statusNorm)) {
                throw new RegraDeNegocioException(
                        "Status do cortejo inválido. Valores aceitos: AGUARDANDO_CHEGADA, NO_LOCAL");
            }
            p.setStatusCortejo(statusNorm);
        } else if (novoStatus) {
            if (p.getStatusCortejo() == null || "AGUARDANDO_CHEGADA".equalsIgnoreCase(p.getStatusCortejo())) {
                p.setStatusCortejo("NO_LOCAL");
            }
        } else {
            p.setStatusCortejo("AGUARDANDO_CHEGADA");
        }

        p.setUpdatedAt(LocalDateTime.now());
        ParticipanteCerimonia salvo = participanteGateway.salvar(p);

        // Sincroniza presença no convite da família
        String pNomeNorm = normalizar(p.getNome());
        if (p.getCodigoConvite() != null && !p.getCodigoConvite().isBlank()) {
            conviteGateway.buscarPorCodigo(p.getCodigoConvite().trim()).ifPresent(c -> {
                atualizarPresencaEmConvite(c, pNomeNorm, novoStatus);
            });
        } else {
            for (Convite c : conviteGateway.listarTodos()) {
                atualizarPresencaEmConvite(c, pNomeNorm, novoStatus);
            }
        }

        return salvo;
    }

    private void atualizarPresencaEmConvite(Convite c, String pNomeNorm, boolean novoStatus) {
        if (c.getMembros() != null) {
            boolean alterou = false;
            for (MembroConvite m : c.getMembros()) {
                if (m.getNome() != null) {
                    String mNorm = normalizar(m.getNome());
                    if (mNorm.equalsIgnoreCase(pNomeNorm) || mNorm.contains(pNomeNorm) || pNomeNorm.contains(mNorm)) {
                        m.setPresenteCheckin(novoStatus);
                        m.setDataHoraCheckin(novoStatus ? LocalDateTime.now() : null);
                        m.setRecepcionista("Cerimonial");
                        alterou = true;
                    }
                }
            }
            if (alterou) {
                c.setUpdatedAt(LocalDateTime.now());
                conviteGateway.salvar(c);
            }
        }
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
