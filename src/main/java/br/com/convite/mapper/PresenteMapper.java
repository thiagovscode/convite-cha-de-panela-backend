package br.com.convite.mapper;

import br.com.convite.domain.Presente;
import br.com.convite.gateway.persistence.entity.PresenteEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PresenteMapper {
    Presente toDomain(PresenteEntity entity);
    PresenteEntity toEntity(Presente domain);
}
