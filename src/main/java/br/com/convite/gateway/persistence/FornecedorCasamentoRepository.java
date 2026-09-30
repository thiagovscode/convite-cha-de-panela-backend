package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FornecedorCasamentoRepository extends MongoRepository<FornecedorCasamentoEntity, String> {
    List<FornecedorCasamentoEntity> findByServicoIgnoreCase(String servico);
    List<FornecedorCasamentoEntity> findByEmpresaContainingIgnoreCaseOrNomeContainingIgnoreCase(String empresa, String nome);
}
