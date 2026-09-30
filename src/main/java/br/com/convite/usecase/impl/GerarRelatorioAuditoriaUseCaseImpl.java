package br.com.convite.usecase.impl;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.gateway.ConviteGateway;
import br.com.convite.usecase.GerarRelatorioAuditoriaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GerarRelatorioAuditoriaUseCaseImpl implements GerarRelatorioAuditoriaUseCase {

    private final ConviteGateway conviteGateway;
    private final br.com.convite.gateway.FornecedorGateway fornecedorGateway;

    public GerarRelatorioAuditoriaUseCaseImpl(ConviteGateway conviteGateway) {
        this(conviteGateway, null);
    }

    @org.springframework.beans.factory.annotation.Autowired
    public GerarRelatorioAuditoriaUseCaseImpl(
            ConviteGateway conviteGateway,
            @org.springframework.beans.factory.annotation.Autowired(required = false) br.com.convite.gateway.FornecedorGateway fornecedorGateway) {
        this.conviteGateway = conviteGateway;
        this.fornecedorGateway = fornecedorGateway;
    }

    @Override
    public RelatorioAuditoria executar() {
        List<Convite> convites = conviteGateway.listarTodos();

        long totalConvidadosPrevistos = 0;
        long totalAdultosPrevistos = 0;
        long totalCriancasPrevistas = 0;

        long totalConfirmadosRsvp = 0;
        long totalAdultosConfirmados = 0;
        long totalCriancasConfirmadas = 0;

        long totalPresentesReais = 0;
        long totalAdultosPresentes = 0;
        long totalCriancasPresentes = 0;

        long totalAusentesNoShow = 0;
        long totalAguardandoChegada = 0;
        long totalRecusados = 0;

        for (Convite c : convites) {
            if (c.getMembros() != null && !c.getMembros().isEmpty()) {
                for (MembroConvite m : c.getMembros()) {
                    totalConvidadosPrevistos++;
                    boolean isCrianca = Boolean.TRUE.equals(m.getCriancaAte6Anos());
                    if (isCrianca) {
                        totalCriancasPrevistas++;
                    } else {
                        totalAdultosPrevistos++;
                    }

                    boolean rsvpOk = Boolean.TRUE.equals(m.getConfirmadoRsvp());
                    if (rsvpOk) {
                        totalConfirmadosRsvp++;
                        if (isCrianca) totalCriancasConfirmadas++;
                        else totalAdultosConfirmados++;

                        if (Boolean.TRUE.equals(m.getPresenteCheckin())) {
                            totalPresentesReais++;
                            if (isCrianca) totalCriancasPresentes++;
                            else totalAdultosPresentes++;
                        } else if (Boolean.FALSE.equals(m.getPresenteCheckin())) {
                            totalAusentesNoShow++;
                        } else {
                            totalAguardandoChegada++;
                        }
                    } else if (Boolean.FALSE.equals(m.getConfirmadoRsvp())
                            || ("RECUSADO".equalsIgnoreCase(c.getStatus()) && m.getConfirmadoRsvp() == null)) {
                        totalRecusados++;
                    }
                }
            } else if ("RECUSADO".equalsIgnoreCase(c.getStatus())) {
                totalRecusados++;
            }
        }

        // Membros de fornecedores que permanecem até o fim contam como convidados
        if (fornecedorGateway != null) {
            for (br.com.convite.domain.Fornecedor f : fornecedorGateway.listarTodos()) {
                if (f.getEquipe() != null) {
                    for (br.com.convite.domain.MembroEquipeFornecedor m : f.getEquipe()) {
                        if (Boolean.TRUE.equals(m.getPermaneceAteFim())) {
                            totalConvidadosPrevistos++;
                            totalAdultosPrevistos++;
                            totalConfirmadosRsvp++;
                            totalAdultosConfirmados++;
                            if (Boolean.TRUE.equals(m.getPresente())) {
                                totalPresentesReais++;
                                totalAdultosPresentes++;
                            } else {
                                totalAguardandoChegada++;
                            }
                        }
                    }
                }
            }
        }

        return new RelatorioAuditoria(
                totalConvidadosPrevistos,
                totalAdultosPrevistos,
                totalCriancasPrevistas,
                totalConfirmadosRsvp,
                totalAdultosConfirmados,
                totalCriancasConfirmadas,
                totalPresentesReais,
                totalAdultosPresentes,
                totalCriancasPresentes,
                totalAusentesNoShow,
                totalAguardandoChegada,
                totalRecusados,
                convites
        );
    }
}
