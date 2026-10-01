package br.com.convite.usecase;

import br.com.convite.domain.Convite;

public interface DefinirParCortejoUseCase {
    /**
     * Define, altera ou desvincula o par de cortejo de um membro de forma recíproca e automática.
     * Atualiza tanto o convite do membro quanto o convite do par (seja no mesmo convite ou em convites distintos),
     * limpando vínculos anteriores para manter a integridade da lista do cortejo.
     *
     * @param codigoConvite Código do convite do membro (opcional se nomeMembro fornecido)
     * @param membroId ID UUID do membro em formato texto (opcional se nomeMembro fornecido)
     * @param nomeMembro Nome do membro para quem se define o par
     * @param nomePar Nome do novo par (se nulo ou em branco, desvincula o par de ambos)
     * @return O convite principal atualizado
     */
    Convite executar(String codigoConvite, String membroId, String nomeMembro, String nomePar);
}
