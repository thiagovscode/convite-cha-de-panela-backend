package br.com.convite.usecase;

import br.com.convite.domain.AcompanhanteCasamento;

import java.util.List;

public interface ProcessarConfirmacaoRsvpCasamentoUseCase {

    ResultadoProcessamentoRsvp executar(
            String codigoConvite,
            String nome,
            String telefone,
            String email,
            Boolean presenca,
            List<AcompanhanteCasamento> acompanhantes,
            String observacao
    );

    record ResultadoProcessamentoRsvp(
            boolean presenca,
            String message,
            int totalPessoas,
            long adultos,
            long criancasAte6Anos
    ) {}
}
