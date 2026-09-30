package br.com.convite.usecase.impl;

import br.com.convite.domain.VinculoParticipante;
import br.com.convite.exception.ConflitoNegocioException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.exception.VinculoNaoEncontradoException;
import br.com.convite.gateway.VinculoGateway;
import br.com.convite.usecase.SalvarVinculoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalvarVinculoUseCaseImpl implements SalvarVinculoUseCase {

    private final VinculoGateway vinculoGateway;

    @Override
    public VinculoParticipante executar(VinculoParticipante dados) {
        if (dados == null || dados.getNome() == null || dados.getNome().trim().isBlank()) {
            throw new RegraDeNegocioException("O nome do vínculo é obrigatório.");
        }

        String nomeLimpo = dados.getNome().trim();
        Optional<VinculoParticipante> existenteComNome = vinculoGateway.buscarPorNome(nomeLimpo);

        LocalDateTime agora = LocalDateTime.now();

        if (dados.getId() != null && !dados.getId().isBlank()) {
            // Atualização
            VinculoParticipante vinculoExistente = vinculoGateway.buscarPorId(dados.getId().trim())
                    .orElseThrow(() -> new VinculoNaoEncontradoException(dados.getId()));

            if (existenteComNome.isPresent() && !existenteComNome.get().getId().equals(vinculoExistente.getId())) {
                throw new ConflitoNegocioException("Já existe um vínculo cadastrado com o nome '" + nomeLimpo + "'.");
            }

            vinculoExistente.setNome(nomeLimpo);
            vinculoExistente.setUpdatedAt(agora);
            return vinculoGateway.salvar(vinculoExistente);
        } else {
            // Criação
            if (existenteComNome.isPresent()) {
                throw new ConflitoNegocioException("Já existe um vínculo cadastrado com o nome '" + nomeLimpo + "'.");
            }

            VinculoParticipante novo = VinculoParticipante.builder()
                    .nome(nomeLimpo)
                    .createdAt(agora)
                    .updatedAt(agora)
                    .build();

            return vinculoGateway.salvar(novo);
        }
    }
}
