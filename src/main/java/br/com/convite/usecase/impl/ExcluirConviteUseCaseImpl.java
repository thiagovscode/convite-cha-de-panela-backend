package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.DesvincularParticipantesCortejoUseCase;
import br.com.convite.usecase.ExcluirConviteUseCase;
import br.com.convite.usecase.ExcluirRsvpPorTelefoneUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ExcluirConviteUseCaseImpl implements ExcluirConviteUseCase {

    private final ConviteGateway conviteGateway;
    private final DesvincularParticipantesCortejoUseCase desvincularParticipantesCortejoUseCase;
    private final ExcluirRsvpPorTelefoneUseCase excluirRsvpPorTelefoneUseCase;
    private final br.com.convite.gateway.RsvpCasamentoGateway rsvpCasamentoGateway;

    @Override
    public Convite executar(String codigoOuId) {
        if (codigoOuId == null || codigoOuId.isBlank()) {
            throw new RegraDeNegocioException("Identificador do convite é obrigatório.");
        }

        String chave = codigoOuId.trim();
        Optional<Convite> opt = conviteGateway.buscarPorCodigo(chave);
        if (opt.isEmpty()) {
            opt = conviteGateway.buscarPorId(chave);
        }

        Convite convite = opt.orElseThrow(() -> new ConviteNaoEncontradoException(chave));

        // 1. Limpa vínculos do cortejo se houver código
        if (convite.getCodigo() != null) {
            desvincularParticipantesCortejoUseCase.executar(convite.getCodigo());
        }

        // 2. Limpa RSVPs associados ao telefone do convite
        if (convite.getTelefone() != null && !convite.getTelefone().isBlank()) {
            excluirRsvpPorTelefoneUseCase.executar(convite.getTelefone());
        }

        // 3. Limpa RSVPs de membros da família caso o telefone não estivesse no convite
        if (convite.getMembros() != null && !convite.getMembros().isEmpty()) {
            try {
                var todosRsvps = rsvpCasamentoGateway.listarTodos();
                for (var m : convite.getMembros()) {
                    if (m.getNome() != null && !m.getNome().isBlank()) {
                        String nomeMembro = m.getNome().trim();
                        todosRsvps.stream()
                                .filter(r -> r.getNome() != null && r.getNome().trim().equalsIgnoreCase(nomeMembro))
                                .forEach(r -> rsvpCasamentoGateway.deletar(r.getId()));
                    }
                }
            } catch (Exception ignored) {
                // Silencioso caso ocorra falha secundária na busca de órfãos
            }
        }

        // 4. Exclui o convite do banco
        conviteGateway.excluir(convite);
        return convite;
    }
}
