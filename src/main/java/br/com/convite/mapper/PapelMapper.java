package br.com.convite.mapper;

import br.com.convite.domain.PapelParticipante;
import br.com.convite.gateway.persistence.entity.PapelParticipanteEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PapelMapper {
    PapelParticipante toDomain(PapelParticipanteEntity entity);
    PapelParticipanteEntity toEntity(PapelParticipante domain);
    List<PapelParticipante> toDomainList(List<PapelParticipanteEntity> entities);
    List<PapelParticipanteEntity> toEntityList(List<PapelParticipante> domains);
}
