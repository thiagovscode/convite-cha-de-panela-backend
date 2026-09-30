package br.com.convite.mapper;

import br.com.convite.domain.ParticipanteCerimonia;
import br.com.convite.gateway.persistence.entity.ParticipanteCerimoniaEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParticipanteCerimoniaMapper {
    ParticipanteCerimonia toDomain(ParticipanteCerimoniaEntity entity);
    ParticipanteCerimoniaEntity toEntity(ParticipanteCerimonia domain);

    List<ParticipanteCerimonia> toDomainList(List<ParticipanteCerimoniaEntity> entities);
    List<ParticipanteCerimoniaEntity> toEntityList(List<ParticipanteCerimonia> domains);
}
