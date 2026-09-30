package br.com.convite.gateway.impl;

import br.com.convite.domain.Fornecedor;
import br.com.convite.gateway.FornecedorGateway;
import br.com.convite.gateway.persistence.FornecedorCasamentoRepository;
import br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity;
import br.com.convite.mapper.FornecedorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FornecedorGatewayImpl implements FornecedorGateway {

    private final FornecedorCasamentoRepository repository;
    private final FornecedorMapper mapper;

    @Override
    public List<Fornecedor> listarTodos() {
        return mapper.toDomainList(repository.findAll());
    }

    @Override
    public Optional<Fornecedor> buscarPorId(String id) {
        if (id == null || id.isBlank()) return Optional.empty();
        return repository.findById(id.trim()).map(mapper::toDomain);
    }

    @Override
    public Fornecedor salvar(Fornecedor fornecedor) {
        FornecedorCasamentoEntity entity = mapper.toEntity(fornecedor);
        FornecedorCasamentoEntity salvo = repository.save(entity);
        return mapper.toDomain(salvo);
    }

    @Override
    public void excluir(String id) {
        if (id != null && !id.isBlank()) {
            repository.deleteById(id.trim());
        }
    }
}
