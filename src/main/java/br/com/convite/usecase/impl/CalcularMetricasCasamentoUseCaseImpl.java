package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.MetricasCasamento;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.CalcularMetricasCasamentoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CalcularMetricasCasamentoUseCaseImpl implements CalcularMetricasCasamentoUseCase {

    private final ConviteGateway conviteGateway;

    @Override
    public MetricasCasamento executar() {
        List<Convite> convites = conviteGateway.listarTodos();

        long totalConvites = convites.size();
        long totalConvitesConfirmados = 0;
        long totalConvitesRecusados = 0;
        long totalConvitesPendentes = 0;

        long totalPessoas = 0;
        long totalConfirmados = 0;
        long totalRecusaram = 0;
        long totalAdultosConfirmados = 0;
        long totalCriancasConfirmadas = 0;

        for (Convite c : convites) {
            String status = c.getStatus() != null ? c.getStatus().toUpperCase() : "PENDENTE";
            if ("CONFIRMADO".equals(status)) {
                totalConvitesConfirmados++;
            } else if ("RECUSADO".equals(status)) {
                totalConvitesRecusados++;
            } else {
                totalConvitesPendentes++;
            }

            if (c.getMembros() != null && !c.getMembros().isEmpty()) {
                for (MembroConvite m : c.getMembros()) {
                    totalPessoas++;
                    boolean isCrianca = Boolean.TRUE.equals(m.getCriancaAte6Anos());
                    if (Boolean.TRUE.equals(m.getConfirmadoRsvp())) {
                        totalConfirmados++;
                        if (isCrianca) {
                            totalCriancasConfirmadas++;
                        } else {
                            totalAdultosConfirmados++;
                        }
                    } else if (Boolean.FALSE.equals(m.getConfirmadoRsvp())
                            || ("RECUSADO".equals(status) && m.getConfirmadoRsvp() == null)) {
                        totalRecusaram++;
                    }
                }
            } else {
                totalPessoas++;
                if ("CONFIRMADO".equals(status)) {
                    totalConfirmados++;
                    totalAdultosConfirmados++;
                } else if ("RECUSADO".equals(status)) {
                    totalRecusaram++;
                }
            }
        }

        long totalPendentes = Math.max(0, totalPessoas - totalConfirmados - totalRecusaram);
        long totalRespondidos = totalConfirmados + totalRecusaram;

        double taxaConfirmacao = totalPessoas > 0 ? Math.round(((double) totalConfirmados / totalPessoas) * 100.0) : 0;
        double taxaRecusa = totalPessoas > 0 ? Math.round(((double) totalRecusaram / totalPessoas) * 100.0) : 0;
        double taxaPendentes = totalPessoas > 0 ? Math.max(0, 100 - taxaConfirmacao - taxaRecusa) : 0;
        double taxaPresencaRespondidos = totalRespondidos > 0 ? Math.round(((double) totalConfirmados / totalRespondidos) * 100.0) : 0;

        return MetricasCasamento.builder()
                .totalConvites(totalConvites)
                .totalConvitesConfirmados(totalConvitesConfirmados)
                .totalConvitesRecusados(totalConvitesRecusados)
                .totalConvitesPendentes(totalConvitesPendentes)
                .totalPessoas(totalPessoas)
                .totalConfirmados(totalConfirmados)
                .totalRecusaram(totalRecusaram)
                .totalPendentes(totalPendentes)
                .totalAdultosConfirmados(totalAdultosConfirmados)
                .totalCriancasConfirmadas(totalCriancasConfirmadas)
                .taxaConfirmacao(taxaConfirmacao)
                .taxaRecusa(taxaRecusa)
                .taxaPendentes(taxaPendentes)
                .taxaPresencaRespondidos(taxaPresencaRespondidos)
                .build();
    }
}
