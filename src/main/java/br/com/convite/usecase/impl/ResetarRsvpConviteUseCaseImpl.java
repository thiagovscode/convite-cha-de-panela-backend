package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.usecase.ResetarRsvpConviteUseCase;
import br.com.convite.usecase.SincronizarCortejoConviteUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResetarRsvpConviteUseCaseImpl implements ResetarRsvpConviteUseCase {

    private final ConviteGateway conviteGateway;
    private final RsvpCasamentoGateway rsvpCasamentoGateway;
    private final SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    @Override
    public Convite executar(String codigoOuId) {
        if (codigoOuId == null || codigoOuId.isBlank()) {
            throw new RegraDeNegocioException("Código ou identificador do convite é obrigatório para resetar o RSVP.");
        }

        String termo = codigoOuId.trim();

        // 1. Localiza o convite estritamente pelo código ou pelo ID
        Convite convite = conviteGateway.buscarPorCodigo(termo)
                .or(() -> conviteGateway.buscarPorId(termo))
                .orElseThrow(() -> new ConviteNaoEncontradoException(termo));

        String codigoConvite = convite.getCodigo();

        // 2. Remove da base qualquer registro de RSVP vinculado a este código de convite
        if (codigoConvite != null && !codigoConvite.isBlank()) {
            rsvpCasamentoGateway.deletarPorCodigoConvite(codigoConvite);
        }

        // 3. Reseta o status estrutural do convite para PENDENTE (como convite novo)
        convite.setStatus("PENDENTE");
        convite.setDataConfirmacao(null);
        convite.setUpdatedAt(LocalDateTime.now());

        // 4. Percorre cada membro dentro do convite (pelo seu ID de membro) e limpa flags de RSVP e portaria
        if (convite.getMembros() != null) {
            for (MembroConvite membro : convite.getMembros()) {
                if (membro.getId() != null) {
                    membro.setConfirmadoRsvp(null);
                    membro.setPresenteCheckin(null);
                    membro.setDataHoraCheckin(null);
                    membro.setRecepcionista(null);
                }
            }
        }

        // 5. Salva o convite resetado
        Convite salvo = conviteGateway.salvar(convite);

        // 6. Sincroniza participantes do cortejo (se houver membros de cortejo neste convite)
        try {
            sincronizarCortejoConviteUseCase.executar(salvo);
        } catch (Exception e) {
            log.warn("Falha ao sincronizar cortejo no reset do convite {}: {}", codigoConvite, e.getMessage());
        }

        log.info("RSVP do convite '{}' (família: '{}') foi resetado com sucesso para PENDENTE.", codigoConvite, salvo.getFamilia());
        return salvo;
    }
}
