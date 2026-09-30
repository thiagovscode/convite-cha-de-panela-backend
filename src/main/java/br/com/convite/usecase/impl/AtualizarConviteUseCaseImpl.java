package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.AtualizarConviteUseCase;
import br.com.convite.usecase.SincronizarCortejoConviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AtualizarConviteUseCaseImpl implements AtualizarConviteUseCase {

    private final ConviteGateway conviteGateway;
    private final SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    @Override
    public Convite executar(Convite dados) {
        if (dados == null) {
            throw new RegraDeNegocioException("Dados para atualização do convite não fornecidos.");
        }

        // Busca assertiva combinando ID e Código do convite
        Convite existente = conviteGateway.buscarPorIdECodigo(dados.getId(), dados.getCodigo())
                .or(() -> conviteGateway.buscarPorCodigoOuId(dados.getCodigo()))
                .or(() -> conviteGateway.buscarPorCodigoOuId(dados.getId()))
                .orElseThrow(() -> new ConviteNaoEncontradoException(
                        dados.getCodigo() != null ? dados.getCodigo() : (dados.getId() != null ? dados.getId() : "desconhecido")));

        if (dados.getFamilia() != null && !dados.getFamilia().trim().isBlank()) {
            existente.setFamilia(dados.getFamilia().trim());
        }

        existente.setTelefone(dados.getTelefone() != null && !dados.getTelefone().isBlank() ? dados.getTelefone().trim() : null);
        existente.setEmail(dados.getEmail() != null && !dados.getEmail().isBlank() ? dados.getEmail().trim() : null);
        existente.setPapel(dados.getPapel() != null && !dados.getPapel().isBlank() ? dados.getPapel().trim() : null);
        existente.setObservacao(dados.getObservacao() != null && !dados.getObservacao().isBlank() ? dados.getObservacao().trim() : null);
        existente.setUpdatedAt(LocalDateTime.now());

        if (dados.getMembros() != null && !dados.getMembros().isEmpty()) {
            existente.setMembros(mesclarMembros(dados.getMembros(), existente.getMembros()));
        }

        Convite atualizado = conviteGateway.salvar(existente);

        // Atualiza participantes do cortejo correspondentes
        sincronizarCortejoConviteUseCase.executar(atualizado);

        return atualizado;
    }

    private List<MembroConvite> mesclarMembros(List<MembroConvite> novos, List<MembroConvite> existentes) {
        Map<String, MembroConvite> mapaExistentesPorId = new HashMap<>();
        Map<String, MembroConvite> mapaExistentesPorNome = new HashMap<>();

        if (existentes != null) {
            for (MembroConvite m : existentes) {
                if (m.getId() != null) mapaExistentesPorId.put(m.getId(), m);
                if (m.getNome() != null) mapaExistentesPorNome.put(m.getNome().trim().toLowerCase(), m);
            }
        }

        List<MembroConvite> resultado = new ArrayList<>();
        for (MembroConvite m : novos) {
            if (m.getNome() == null || m.getNome().trim().isBlank()) continue;

            String id = (m.getId() != null && !m.getId().isBlank()) ? m.getId().trim() : null;
            MembroConvite antigo = null;
            if (id != null) {
                antigo = mapaExistentesPorId.get(id);
            }
            if (antigo == null) {
                antigo = mapaExistentesPorNome.get(m.getNome().trim().toLowerCase());
            }

            String finalId = id != null ? id : (antigo != null && antigo.getId() != null ? antigo.getId() : UUID.randomUUID().toString());

            resultado.add(MembroConvite.builder()
                    .id(finalId)
                    .nome(m.getNome().trim())
                    .criancaAte6Anos(Boolean.TRUE.equals(m.getCriancaAte6Anos()))
                    .papel(m.getPapel() != null ? m.getPapel().trim() : null)
                    .vinculo(m.getVinculo() != null ? m.getVinculo().trim() : null)
                    .par(m.getPar() != null && !m.getPar().isBlank() ? m.getPar().trim() : (antigo != null ? antigo.getPar() : null))
                    .participaCortejo(m.getParticipaCortejo() != null ? m.getParticipaCortejo() : (antigo != null ? antigo.getParticipaCortejo() : null))
                    .confirmadoRsvp(antigo != null ? antigo.getConfirmadoRsvp() : null)
                    .presenteCheckin(antigo != null ? antigo.getPresenteCheckin() : null)
                    .dataHoraCheckin(antigo != null ? antigo.getDataHoraCheckin() : null)
                    .recepcionista(antigo != null ? antigo.getRecepcionista() : null)
                    .build());
        }

        if (resultado.isEmpty()) {
            throw new RegraDeNegocioException("Pelo menos um membro válido com nome preenchido deve ser mantido.");
        }

        return resultado;
    }
}
