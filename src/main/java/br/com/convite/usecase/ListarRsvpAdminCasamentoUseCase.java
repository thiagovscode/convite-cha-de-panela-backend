package br.com.convite.usecase;

import java.time.LocalDateTime;
import java.util.List;

public interface ListarRsvpAdminCasamentoUseCase {

    record AcompanhanteItem(
            String id,
            String nome,
            Boolean criancaAte6Anos
    ) {}

    record RsvpAdminItem(
            String id,
            String nome,
            String telefone,
            String email,
            Boolean presenca,
            List<AcompanhanteItem> acompanhantes,
            String observacao,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            int totalPessoas,
            long adultos,
            long criancasAte6Anos
    ) {}

    record ResumoGeral(
            long totalRsvps,
            long totalConfirmados,
            long totalRecusaram,
            long totalAdultos,
            long totalCriancasAte6Anos
    ) {}

    record RelatorioRsvpAdmin(List<RsvpAdminItem> data, ResumoGeral resumoGeral) {}

    RelatorioRsvpAdmin executar();
}

