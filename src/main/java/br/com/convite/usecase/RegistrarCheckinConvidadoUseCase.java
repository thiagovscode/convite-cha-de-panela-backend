package br.com.convite.usecase;

import br.com.convite.domain.Convite;

import java.util.List;

public interface RegistrarCheckinConvidadoUseCase {

    record PresencaMembro(String membroId, Boolean presente) {}

    record Comando(String codigo, String recepcionista, List<PresencaMembro> presencas) {}

    record ResultadoCheckinConvidado(Convite convite, int presentes, int ausentes) {}

    ResultadoCheckinConvidado executar(Comando comando);
}

