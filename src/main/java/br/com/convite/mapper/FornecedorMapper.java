package br.com.convite.mapper;

import br.com.convite.domain.Fornecedor;
import br.com.convite.domain.MembroEquipeFornecedor;
import br.com.convite.gateway.persistence.entity.FornecedorCasamentoEntity;
import br.com.convite.gateway.persistence.entity.MembroEquipeFornecedorEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FornecedorMapper {
    Fornecedor toDomain(FornecedorCasamentoEntity entity);
    FornecedorCasamentoEntity toEntity(Fornecedor domain);

    MembroEquipeFornecedor toDomain(MembroEquipeFornecedorEntity entity);
    MembroEquipeFornecedorEntity toEntity(MembroEquipeFornecedor domain);

    List<Fornecedor> toDomainList(List<FornecedorCasamentoEntity> entities);
    List<FornecedorCasamentoEntity> toEntityList(List<Fornecedor> domains);
}
