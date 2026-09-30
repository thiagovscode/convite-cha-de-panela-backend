package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.usecase.ExcluirRsvpPorTelefoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcluirRsvpPorTelefoneUseCaseImpl implements ExcluirRsvpPorTelefoneUseCase {

    private final RsvpCasamentoGateway rsvpCasamentoGateway;
    private final ConviteGateway conviteGateway;

    @Override
    public void executar(String telefone) {
        if (telefone == null || telefone.isBlank()) return;

        String telLimpo = telefone.replaceAll("\\D", "");

        // Remove o RSVP (tenta com formato original e formato normalizado)
        rsvpCasamentoGateway.buscarPorTelefone(telefone).ifPresent(r -> rsvpCasamentoGateway.deletar(r.getId()));
        if (!telLimpo.isBlank() && !telLimpo.equals(telefone)) {
            rsvpCasamentoGateway.buscarPorTelefone(telLimpo).ifPresent(r -> rsvpCasamentoGateway.deletar(r.getId()));
        }

        // Sincroniza o convite associado: reseta status e confirmações dos membros
        Optional<Convite> conviteOpt = conviteGateway.listarTodos().stream()
                .filter(c -> telefone.equals(c.getTelefone()) ||
                             telLimpo.equals(c.getTelefone()) ||
                             (c.getTelefone() != null && telLimpo.equals(c.getTelefone().replaceAll("\\D", ""))))
                .findFirst();

        conviteOpt.ifPresent(convite -> {
            convite.setStatus("PENDENTE");
            convite.setDataConfirmacao(null);
            convite.setUpdatedAt(LocalDateTime.now());
            if (convite.getMembros() != null) {
                for (MembroConvite m : convite.getMembros()) {
                    m.setConfirmadoRsvp(null);
                }
            }
            conviteGateway.salvar(convite);
        });
    }
}
