package br.com.convite.mapper;

import br.com.convite.domain.AcompanhanteCasamento;
import br.com.convite.domain.RsvpCasamento;
import br.com.convite.gateway.persistence.entity.AcompanhanteCasamentoEntity;
import br.com.convite.gateway.persistence.entity.RsvpCasamentoEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RsvpCasamentoMapper {
    RsvpCasamento toDomain(RsvpCasamentoEntity entity);
    RsvpCasamentoEntity toEntity(RsvpCasamento domain);

    AcompanhanteCasamento toDomain(AcompanhanteCasamentoEntity entity);
    AcompanhanteCasamentoEntity toEntity(AcompanhanteCasamento domain);
}