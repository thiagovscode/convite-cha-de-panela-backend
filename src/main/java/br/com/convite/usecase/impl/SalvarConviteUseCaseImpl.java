package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.AtualizarConviteUseCase;
import br.com.convite.usecase.CriarConviteUseCase;
import br.com.convite.usecase.SalvarConviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Facade unificada para persistência de convites.
 * Delega para CriarConviteUseCase ou AtualizarConviteUseCase com base na presença de ID ou Código existente.
 */
@Service
@RequiredArgsConstructor
public class SalvarConviteUseCaseImpl implements SalvarConviteUseCase {

    private final CriarConviteUseCase criarConviteUseCase;
    private final AtualizarConviteUseCase atualizarConviteUseCase;
    private final ConviteGateway conviteGateway;

    @Override
    public Convite executar(Convite dados) {
        if (dados == null) return null;

        // Se possui ID ou se o código informado já existe no banco, trata como ATUALIZAÇÃO
        boolean isAtualizacao = false;
        if (dados.getId() != null && !dados.getId().isBlank()) {
            isAtualizacao = conviteGateway.buscarPorId(dados.getId().trim()).isPresent();
        }
        if (!isAtualizacao && dados.getCodigo() != null && !dados.getCodigo().isBlank()) {
            isAtualizacao = conviteGateway.buscarPorCodigo(dados.getCodigo().trim()).isPresent();
        }

        if (isAtualizacao) {
            return atualizarConviteUseCase.executar(dados);
        } else {
            return criarConviteUseCase.executar(dados);
        }
    }
}
