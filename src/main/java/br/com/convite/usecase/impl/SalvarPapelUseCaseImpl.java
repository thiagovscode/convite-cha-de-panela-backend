package br.com.convite.usecase.impl;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.exception.ConflitoNegocioException;
import br.com.convite.exception.PapelNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.PapelGateway;
import br.com.convite.usecase.SalvarPapelUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SalvarPapelUseCaseImpl implements SalvarPapelUseCase {

    private final PapelGateway papelGateway;

    @Override
    public PapelParticipante executar(PapelParticipante dados) {
        if (dados == null || dados.getNome() == null || dados.getNome().trim().isBlank()) {
            throw new RegraDeNegocioException("O nome do papel é obrigatório.");
        }

        String nomeLimpo = dados.getNome().trim();
        Optional<PapelParticipante> existenteComNome = papelGateway.buscarPorNome(nomeLimpo);

        LocalDateTime agora = LocalDateTime.now();

        if (dados.getId() != null && !dados.getId().isBlank()) {
            // Atualização
            PapelParticipante papelExistente = papelGateway.buscarPorId(dados.getId().trim())
                    .orElseThrow(() -> new PapelNaoEncontradoException(dados.getId()));

            if (existenteComNome.isPresent() && !existenteComNome.get().getId().equals(papelExistente.getId())) {
                throw new ConflitoNegocioException("Já existe um papel cadastrado com o nome '" + nomeLimpo + "'.");
            }

            papelExistente.setNome(nomeLimpo);
            papelExistente.setCortejo(Boolean.TRUE.equals(dados.getCortejo()));
            papelExistente.setUpdatedAt(agora);
            return papelGateway.salvar(papelExistente);
        } else {
            // Criação
            if (existenteComNome.isPresent()) {
                throw new ConflitoNegocioException("Já existe um papel cadastrado com o nome '" + nomeLimpo + "'.");
            }

            PapelParticipante novo = PapelParticipante.builder()
                    .nome(nomeLimpo)
                    .cortejo(Boolean.TRUE.equals(dados.getCortejo()))
                    .createdAt(agora)
                    .updatedAt(agora)
                    .build();

            return papelGateway.salvar(novo);
        }
    }
}
