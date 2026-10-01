package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.gateway.PapelGateway;
import br.com.convite.gateway.ParticipanteCerimoniaGateway;
import br.com.convite.usecase.SincronizarCortejoConviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SincronizarCortejoConviteUseCaseImpl implements SincronizarCortejoConviteUseCase {

    private final ParticipanteCerimoniaGateway participanteGateway;
    private final PapelGateway papelGateway;

    private static final Set<String> PAPEIS_CORTEJO_PADRAO = Set.of(
            "PADRINHO", "MADRINHA", "PAI", "MAE", "MÃE", "DAMINHA", "DAMA", "FLORISTA", "PAJEM"
    );

    @Override
    public void executar(Convite convite) {
        if (convite == null || convite.getMembros() == null || convite.getMembros().isEmpty()) {
            return;
        }

        String codigoConvite = convite.getCodigo();
        List<ParticipanteCerimonia> participantesExistentes = (codigoConvite != null && !codigoConvite.isBlank())
                ? participanteGateway.buscarPorCodigoConvite(codigoConvite)
                : Collections.emptyList();

        Map<String, ParticipanteCerimonia> mapaPorNome = new HashMap<>();
        for (ParticipanteCerimonia p : participantesExistentes) {
            if (p.getNome() != null) {
                mapaPorNome.put(normalizar(p.getNome()), p);
            }
        }

        LocalDateTime agora = LocalDateTime.now();
        boolean conviteRecusado = "RECUSADO".equalsIgnoreCase(convite.getStatus());

        for (MembroConvite membro : convite.getMembros()) {
            if (membro.getNome() == null || membro.getNome().trim().isBlank()) continue;

            String papel = (membro.getPapel() != null && !membro.getPapel().isBlank())
                    ? membro.getPapel().trim()
                    : "Convidado";

            boolean deveParticipar;
            if (membro.getParticipaCortejo() != null) {
                deveParticipar = Boolean.TRUE.equals(membro.getParticipaCortejo());
            } else {
                deveParticipar = isPapelCortejo(papel);
            }

            String nomeNorm = normalizar(membro.getNome());
            ParticipanteCerimonia participante = mapaPorNome.get(nomeNorm);

            if (!deveParticipar) {
                if (participante != null && participante.getId() != null) {
                    participanteGateway.excluirPorId(participante.getId());
                }
                continue;
            }

            boolean isPresente = Boolean.TRUE.equals(membro.getPresenteCheckin());
            Boolean rsvpOk = conviteRecusado ? Boolean.FALSE : membro.getConfirmadoRsvp();

            String finalPar = (membro.getPar() != null && !membro.getPar().isBlank()) ? membro.getPar().trim() : null;

            if (participante != null) {
                // Atualiza existente
                participante.setNome(membro.getNome().trim());
                if (membro.getPapel() != null && !membro.getPapel().isBlank()) {
                    participante.setPapel(membro.getPapel().trim());
                }
                participante.setPar(finalPar);
                participante.setTelefone(convite.getTelefone());
                participante.setConfirmadoRsvp(rsvpOk);
                participante.setPresenteCheckin(isPresente);
                if (isPresente && participante.getDataHoraEntrada() == null) {
                    participante.setDataHoraEntrada(membro.getDataHoraCheckin() != null ? membro.getDataHoraCheckin() : agora);
                }
                if (isPresente && (participante.getStatusCortejo() == null || "AGUARDANDO_CHEGADA".equalsIgnoreCase(participante.getStatusCortejo()))) {
                    participante.setStatusCortejo("NO_LOCAL");
                }
                participante.setUpdatedAt(agora);
                participanteGateway.salvar(participante);
            } else {
                // Cria novo integrante do cortejo
                ParticipanteCerimonia novo = ParticipanteCerimonia.builder()
                        .id(UUID.randomUUID().toString())
                        .nome(membro.getNome().trim())
                        .papel(membro.getPapel() != null && !membro.getPapel().isBlank() ? membro.getPapel().trim() : papel)
                        .par(finalPar)
                        .codigoConvite(codigoConvite)
                        .telefone(convite.getTelefone())
                        .confirmadoRsvp(rsvpOk)
                        .presenteCheckin(isPresente)
                        .dataHoraEntrada(isPresente ? (membro.getDataHoraCheckin() != null ? membro.getDataHoraCheckin() : agora) : null)
                        .statusCortejo(isPresente ? "NO_LOCAL" : "AGUARDANDO_CHEGADA")
                        .createdAt(agora)
                        .updatedAt(agora)
                        .build();
                participanteGateway.salvar(novo);
            }
        }
    }

    private boolean isPapelCortejo(String papel) {
        if (papel == null || papel.isBlank()) return false;
        String nomeLimpo = papel.trim();

        // 1. Tenta verificar na base de dados se está configurado como cortejo
        var opt = papelGateway.buscarPorNome(nomeLimpo);
        if (opt.isPresent()) {
            return Boolean.TRUE.equals(opt.get().getCortejo());
        }

        // 2. Fallback de segurança para papéis canônicos de cortejo
        String pUpper = normalizar(papel).toUpperCase();
        return PAPEIS_CORTEJO_PADRAO.stream().anyMatch(pUpper::contains);
    }

    private String normalizar(String s) {
        if (s == null) return "";
        return Normalizer.normalize(s.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }
}
