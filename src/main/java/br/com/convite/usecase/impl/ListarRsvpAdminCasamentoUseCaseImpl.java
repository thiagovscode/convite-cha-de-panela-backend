package br.com.convite.usecase.impl;

import br.com.convite.domain.AcompanhanteCasamento;
import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.domain.RsvpCasamento;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.gateway.RsvpCasamentoGateway;
import br.com.convite.usecase.ListarRsvpAdminCasamentoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ListarRsvpAdminCasamentoUseCaseImpl implements ListarRsvpAdminCasamentoUseCase {

    private final RsvpCasamentoGateway rsvpCasamentoGateway;
    private final ConviteGateway conviteGateway;

    @Override
    public RelatorioRsvpAdmin executar() {
        List<RsvpCasamento> rsvps = rsvpCasamentoGateway.listarTodos();

        List<RsvpAdminItem> data = rsvps.stream()
                .map(this::toAdminItem)
                .collect(Collectors.toList());

        ResumoGeral resumo = calcularResumoGeral(rsvps);
        return new RelatorioRsvpAdmin(data, resumo);
    }

    private RsvpAdminItem toAdminItem(RsvpCasamento rsvp) {
        List<AcompanhanteItem> acompanhantes = null;
        if (rsvp.getAcompanhantes() != null) {
            acompanhantes = rsvp.getAcompanhantes().stream()
                    .map(a -> new AcompanhanteItem(
                            a.getId(),
                            a.getNome(),
                            a.getCriancaAte6Anos()
                    ))
                    .collect(Collectors.toList());
        }

        int totalPessoas = 1 + (rsvp.getAcompanhantes() != null ? rsvp.getAcompanhantes().size() : 0);
        long adultos = Boolean.TRUE.equals(rsvp.getPresenca())
                ? 1 + (rsvp.getAcompanhantes() != null ? rsvp.getAcompanhantes().stream().filter(a -> !Boolean.TRUE.equals(a.getCriancaAte6Anos())).count() : 0)
                : 0;
        long criancas = (Boolean.TRUE.equals(rsvp.getPresenca()) && rsvp.getAcompanhantes() != null)
                ? rsvp.getAcompanhantes().stream().filter(a -> Boolean.TRUE.equals(a.getCriancaAte6Anos())).count()
                : 0;

        return new RsvpAdminItem(
                rsvp.getId(),
                rsvp.getNome(),
                rsvp.getTelefone(),
                rsvp.getEmail(),
                rsvp.getPresenca(),
                acompanhantes,
                rsvp.getObservacao(),
                rsvp.getCreatedAt(),
                rsvp.getUpdatedAt(),
                totalPessoas,
                adultos,
                criancas
        );
    }

    private ResumoGeral calcularResumoGeral(List<RsvpCasamento> rsvps) {
        long totalConvitesCadastrados = conviteGateway.contarTotal();

        if (totalConvitesCadastrados > 0) {
            List<Convite> convites = conviteGateway.listarTodos();
            long totalConfirmados = 0;
            long totalRecusaram = 0;
            long totalAdultos = 0;
            long totalCriancasAte6Anos = 0;

            for (Convite c : convites) {
                if (c.getMembros() != null) {
                    for (MembroConvite m : c.getMembros()) {
                        boolean isCrianca = Boolean.TRUE.equals(m.getCriancaAte6Anos());
                        if (Boolean.TRUE.equals(m.getConfirmadoRsvp())) {
                            totalConfirmados++;
                            if (isCrianca) totalCriancasAte6Anos++;
                            else totalAdultos++;
                        } else if (Boolean.FALSE.equals(m.getConfirmadoRsvp()) || ("RECUSADO".equalsIgnoreCase(c.getStatus()) && m.getConfirmadoRsvp() == null)) {
                            totalRecusaram++;
                        }
                    }
                } else if ("RECUSADO".equalsIgnoreCase(c.getStatus())) {
                    totalRecusaram++;
                }
            }

            return new ResumoGeral(
                    convites.size(),
                    totalConfirmados,
                    totalRecusaram,
                    totalAdultos,
                    totalCriancasAte6Anos
            );
        }

        long totalConfirmados = rsvps.stream().filter(r -> Boolean.TRUE.equals(r.getPresenca())).count();
        long totalRecusaram = rsvps.stream().filter(r -> Boolean.FALSE.equals(r.getPresenca())).count();
        long totalAdultos = 0;
        long totalCriancas = 0;

        for (RsvpCasamento r : rsvps) {
            if (Boolean.TRUE.equals(r.getPresenca())) {
                totalAdultos++;
                if (r.getAcompanhantes() != null) {
                    for (AcompanhanteCasamento a : r.getAcompanhantes()) {
                        if (Boolean.TRUE.equals(a.getCriancaAte6Anos())) totalCriancas++;
                        else totalAdultos++;
                    }
                }
            }
        }

        return new ResumoGeral(
                rsvps.size(),
                totalConfirmados,
                totalRecusaram,
                totalAdultos,
                totalCriancas
        );
    }
}
