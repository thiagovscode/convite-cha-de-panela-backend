package br.com.convite.usecase.impl;

import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.usecase.ExcluirRsvpPorTelefoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExcluirRsvpPorTelefoneUseCaseImpl implements ExcluirRsvpPorTelefoneUseCase {

    private final RsvpCasamentoGateway rsvpCasamentoGateway;

    @Override
    public void executar(String telefone) {
        if (telefone == null || telefone.isBlank()) return;

        String telLimpo = telefone.replaceAll("\\D", "");
        rsvpCasamentoGateway.buscarPorTelefone(telefone).ifPresent(r -> rsvpCasamentoGateway.deletar(r.getId()));
        if (!telLimpo.isBlank() && !telLimpo.equals(telefone)) {
            rsvpCasamentoGateway.buscarPorTelefone(telLimpo).ifPresent(r -> rsvpCasamentoGateway.deletar(r.getId()));
        }
    }
}
