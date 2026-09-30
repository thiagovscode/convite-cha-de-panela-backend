package br.com.convite.usecase.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.exception.FornecedorNaoEncontradoException;
import br.com.convite.exception.RegraDeNegocioException;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.usecase.AtualizarFornecedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AtualizarFornecedorUseCaseImpl implements AtualizarFornecedorUseCase {

    private final FornecedorGateway fornecedorGateway;

    @Override
    public Fornecedor executar(String id, Fornecedor dados) {
        if (id == null || id.isBlank()) {
            throw new RegraDeNegocioException("Identificador do fornecedor é obrigatório.");
        }

        Fornecedor entity = fornecedorGateway.buscarPorId(id.trim())
                .orElseThrow(() -> new FornecedorNaoEncontradoException(id));

        if (dados.getEmpresa() != null && !dados.getEmpresa().isBlank()) entity.setEmpresa(dados.getEmpresa().trim());
        if (dados.getNome() != null && !dados.getNome().isBlank()) {
            entity.setNome(dados.getNome().trim());
            entity.setResponsavel(dados.getNome().trim());
        }
        if (dados.getCategoria() != null) entity.setCategoria(dados.getCategoria().trim());
        if (dados.getServico() != null) entity.setServico(dados.getServico().trim());
        if (dados.getTelefone() != null) entity.setTelefone(dados.getTelefone().trim());
        if (dados.getHorarioPrevisto() != null) entity.setHorarioPrevisto(dados.getHorarioPrevisto().trim());
        if (dados.getInstrucaoChegada() != null) entity.setInstrucaoChegada(dados.getInstrucaoChegada().trim());
        if (dados.getChegadaAntecipada() != null) entity.setChegadaAntecipada(dados.getChegadaAntecipada());
        entity.setUpdatedAt(LocalDateTime.now());

        return fornecedorGateway.salvar(entity);
    }
}
