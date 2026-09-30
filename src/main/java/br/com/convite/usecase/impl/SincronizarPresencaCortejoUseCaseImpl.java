package br.com.convite.usecase.impl;

import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.SincronizarPresencaCortejoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SincronizarPresencaCortejoUseCaseImpl implements SincronizarPresencaCortejoUseCase {

    private final ParticipanteCerimoniaGateway participanteGateway;

    @Override
    public void executar(String codigoConvite, MembroConvite membro, boolean presente, LocalDateTime agora) {
        if (membro == null || membro.getNome() == null) return;
        String nomeNorm = normalizar(membro.getNome());

        List<ParticipanteCerimonia> todos = participanteGateway.listarTodos();
        for (ParticipanteCerimonia p : todos) {
            boolean mesmoConvite = p.getCodigoConvite() != null && p.getCodigoConvite().equalsIgnoreCase(codigoConvite);
            String pNomeNorm = normalizar(p.getNome());
            boolean mesmoNome = pNomeNorm.equalsIgnoreCase(nomeNorm) || pNomeNorm.contains(nomeNorm) || nomeNorm.contains(pNomeNorm);

            if ((mesmoConvite && mesmoNome) || mesmoNome) {
                p.setPresenteCheckin(presente);
                p.setDataHoraEntrada(presente ? agora : null);
                if (presente) {
                    if (p.getStatusCortejo() == null || "AGUARDANDO_CHEGADA".equalsIgnoreCase(p.getStatusCortejo())) {
                        p.setStatusCortejo("NO_LOCAL");
                    }
                } else {
                    p.setStatusCortejo("AGUARDANDO_CHEGADA");
                }
                if (p.getCodigoConvite() == null && codigoConvite != null) {
                    p.setCodigoConvite(codigoConvite);
                }
                p.setUpdatedAt(agora);
                participanteGateway.salvar(p);
            }
        }
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
