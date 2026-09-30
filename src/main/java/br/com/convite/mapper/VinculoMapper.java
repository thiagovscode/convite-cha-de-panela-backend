package br.com.convite.mapper;

import br.com.convite.domain.VinculoParticipante;
import br.com.convite.gateway.persistence.entity.VinculoParticipanteEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VinculoMapper {
    VinculoParticipante toDomain(VinculoParticipanteEntity entity);
    VinculoParticipanteEntity toEntity(VinculoParticipante domain);
    List<VinculoParticipante> toDomainList(List<VinculoParticipanteEntity> entities);
    List<VinculoParticipanteEntity> toEntityList(List<VinculoParticipante> domains);
}
