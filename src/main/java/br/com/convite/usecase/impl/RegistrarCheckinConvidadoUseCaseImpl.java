package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.CheckinDuplicadoException;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.RegistrarCheckinConvidadoUseCase;
import br.com.convite.usecase.SincronizarPresencaCortejoUseCase;
import br.com.convite.usecase.SincronizarPresencaFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RegistrarCheckinConvidadoUseCaseImpl implements RegistrarCheckinConvidadoUseCase {

    private final ConviteGateway conviteGateway;
    private final SincronizarPresencaCortejoUseCase sincronizarPresencaCortejoUseCase;
    private final SincronizarPresencaFornecedorUseCase sincronizarPresencaFornecedorUseCase;

    @Override
    public ResultadoCheckinConvidado executar(Comando comando) {
        if (comando == null || comando.codigo() == null || comando.codigo().isBlank()) {
            throw new RegraDeNegocioException("Código do convite é obrigatório.");
        }

        Convite convite = conviteGateway.buscarPorCodigo(comando.codigo().trim())
                .orElseThrow(() -> new ConviteNaoEncontradoException(comando.codigo()));

        if (convite.getMembros() == null || convite.getMembros().isEmpty()) {
            throw new RegraDeNegocioException("Convite não possui membros cadastrados");
        }

        // Validação de Duplicidade: Impede registrar se todos já entraram
        boolean todosJaEstavamNoEvento = convite.getMembros().stream()
                .allMatch(m -> Boolean.TRUE.equals(m.getPresenteCheckin()));

        boolean algumaNovaEntrada = comando.presencas() != null && comando.presencas().stream()
                .anyMatch(p -> Boolean.TRUE.equals(p.presente()) &&
                        convite.getMembros().stream().anyMatch(m -> m.getId().equals(p.membroId()) && !Boolean.TRUE.equals(m.getPresenteCheckin())));

        if (todosJaEstavamNoEvento && !algumaNovaEntrada) {
            throw new CheckinDuplicadoException();
        }

        Map<String, Boolean> mapaPresenca = (comando.presencas() == null) ? Map.of() : comando.presencas().stream()
                .collect(Collectors.toMap(
                        PresencaMembro::membroId,
                        p -> Boolean.TRUE.equals(p.presente()),
                        (a, b) -> b
                ));

        LocalDateTime agora = LocalDateTime.now();
        String operador = (comando.recepcionista() != null && !comando.recepcionista().isBlank())
                ? comando.recepcionista() : "Recepção";

        int totalPresentes = 0;
        int totalAusentes = 0;

        for (MembroConvite membro : convite.getMembros()) {
            if (mapaPresenca.containsKey(membro.getId())) {
                boolean presente = mapaPresenca.get(membro.getId());
                membro.setPresenteCheckin(presente);
                membro.setRecepcionista(operador);
                if (presente) {
                    membro.setDataHoraCheckin(agora);
                    totalPresentes++;
                } else {
                    membro.setDataHoraCheckin(null);
                    totalAusentes++;
                }

                // Delega sincronizações especializadas aos use cases
                sincronizarPresencaCortejoUseCase.executar(convite.getCodigo(), membro, presente, agora);
                sincronizarPresencaFornecedorUseCase.executar(membro, presente, agora);
            }
        }

        convite.setUpdatedAt(agora);
        Convite salvo = conviteGateway.salvar(convite);
        return new ResultadoCheckinConvidado(salvo, totalPresentes, totalAusentes);
    }
}
