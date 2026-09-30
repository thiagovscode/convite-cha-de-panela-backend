package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.GerarCodigoConviteUnicoUseCase;
import br.com.convite.usecase.SalvarConviteUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SalvarConviteUseCaseImpl implements SalvarConviteUseCase {

    private final ConviteGateway conviteGateway;
    private final GerarCodigoConviteUnicoUseCase gerarCodigoConviteUnicoUseCase;
    private final br.com.convite.usecase.SincronizarCortejoConviteUseCase sincronizarCortejoConviteUseCase;

    @Override
    public Convite executar(Convite dados) {
        if (dados.getFamilia() == null || dados.getFamilia().trim().isBlank()) {
            throw new RegraDeNegocioException("O nome da família ou do convidado principal é obrigatório.");
        }

        // Gera código único somente se não tiver sido informado
        String codigo = (dados.getCodigo() == null || dados.getCodigo().trim().isBlank())
                ? gerarCodigoConviteUnicoUseCase.executar()
                : dados.getCodigo().trim().toLowerCase().replaceAll("[^a-z0-9-_]", "");

        Convite convite = conviteGateway.buscarPorCodigo(codigo)
                .orElseGet(() -> Convite.builder()
                        .codigo(codigo)
                        .createdAt(LocalDateTime.now())
                        .status("PENDENTE")
                        .build());

        convite.setFamilia(dados.getFamilia().trim());
        convite.setTelefone(dados.getTelefone() != null ? dados.getTelefone().trim() : null);
        convite.setEmail(dados.getEmail() != null ? dados.getEmail().trim() : null);
        convite.setPapel(dados.getPapel() != null && !dados.getPapel().trim().isBlank() ? dados.getPapel().trim() : null);
        convite.setObservacao(dados.getObservacao() != null ? dados.getObservacao().trim() : null);
        convite.setUpdatedAt(LocalDateTime.now());

        if (convite.getStatus() == null || convite.getStatus().isBlank()) {
            convite.setStatus("PENDENTE");
        }

        convite.setMembros(processarMembros(dados.getMembros(), convite.getMembros()));
        Convite salvo = conviteGateway.salvar(convite);

        // Sincroniza membros do cortejo automaticamente
        sincronizarCortejoConviteUseCase.executar(salvo);

        return salvo;
    }

    private List<MembroConvite> processarMembros(List<MembroConvite> novos, List<MembroConvite> existentes) {
        if (novos == null || novos.isEmpty()) {
            throw new RegraDeNegocioException("Pelo menos um membro válido deve ser cadastrado.");
        }

        Map<String, MembroConvite> mapaExistente = new HashMap<>();
        if (existentes != null) {
            for (MembroConvite m : existentes) {
                if (m.getId() != null) mapaExistente.put(m.getId(), m);
            }
        }

        List<MembroConvite> resultado = new ArrayList<>();

        for (MembroConvite m : novos) {
            if (m.getNome() == null || m.getNome().trim().isBlank()) continue;

            String id = (m.getId() != null && !m.getId().isBlank()) ? m.getId().trim() : UUID.randomUUID().toString();
            MembroConvite antigo = mapaExistente.get(id);

            resultado.add(MembroConvite.builder()
                    .id(id)
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
            throw new RegraDeNegocioException("Pelo menos um membro válido com nome preenchido deve ser cadastrado.");
        }

        return resultado;
    }
}
