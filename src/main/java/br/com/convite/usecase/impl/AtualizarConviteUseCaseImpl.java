package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.ConviteNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.AtualizarConviteUseCase;
import br.com.convite.usecase.DefinirParCortejoUseCase;
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
    private final DefinirParCortejoUseCase definirParCortejoUseCase;

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
        existente.setObservacao(dados.getObservacao() != null && !dados.getObservacao().isBlank() ? dados.getObservacao().trim() : null);
        existente.setUpdatedAt(LocalDateTime.now());

        if (dados.getMembros() != null && !dados.getMembros().isEmpty()) {
            existente.setMembros(mesclarMembros(dados.getMembros(), existente.getMembros()));
        }

        Convite atualizado = conviteGateway.salvar(existente);

        // Atualiza participantes do cortejo correspondentes
        sincronizarCortejoConviteUseCase.executar(atualizado);

        // Sincroniza pares recíprocos se informados
        if (atualizado.getMembros() != null) {
            for (MembroConvite m : atualizado.getMembros()) {
                if (m.getPar() != null && !m.getPar().isBlank()) {
                    definirParCortejoUseCase.executar(atualizado.getCodigo(), m.getId() != null ? m.getId().toString() : null, m.getNome(), m.getPar());
                }
            }
        }

        return atualizado;
    }

    private List<MembroConvite> mesclarMembros(List<MembroConvite> novos, List<MembroConvite> existentes) {
        Map<String, MembroConvite> mapaExistentesPorId = new HashMap<>();
        Map<String, MembroConvite> mapaExistentesPorNome = new HashMap<>();

        if (existentes != null) {
            for (MembroConvite m : existentes) {
                if (m.getId() != null) mapaExistentesPorId.put(m.getId().toString(), m);
                if (m.getNome() != null) mapaExistentesPorNome.put(m.getNome().trim().toLowerCase(), m);
            }
        }

        List<MembroConvite> resultado = new ArrayList<>();
        for (MembroConvite m : novos) {
            if (m.getNome() == null || m.getNome().trim().isBlank()) continue;

            String idInformado = (m.getId() != null) ? m.getId().toString() : null;
            MembroConvite antigo = null;
            if (idInformado != null) {
                antigo = mapaExistentesPorId.get(idInformado);
            }
            if (antigo == null) {
                antigo = mapaExistentesPorNome.get(m.getNome().trim().toLowerCase());
            }

            UUID finalId = m.getId() != null ? m.getId() : (antigo != null && antigo.getId() != null ? antigo.getId() : UUID.randomUUID());

            resultado.add(MembroConvite.builder()
                    .id(finalId)
                    .nome(m.getNome().trim())
                    .criancaAte6Anos(Boolean.TRUE.equals(m.getCriancaAte6Anos()))
                    .papel(m.getPapel() != null && !m.getPapel().isBlank() ? m.getPapel() : (antigo != null ? antigo.getPapel() : null))
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
