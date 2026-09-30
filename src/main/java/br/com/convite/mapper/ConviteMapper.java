package br.com.convite.mapper;

import br.com.convite.domain.Convite;
import br.com.convite.domain.MembroConvite;
import br.com.convite.gateway.persistence.entity.ConviteCasamentoEntity;
import br.com.convite.gateway.persistence.entity.MembroConviteEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConviteMapper {
    Convite toDomain(ConviteCasamentoEntity entity);
    ConviteCasamentoEntity toEntity(Convite domain);

    MembroConvite toDomain(MembroConviteEntity entity);
    MembroConviteEntity toEntity(MembroConvite domain);

    List<Convite> toDomainList(List<ConviteCasamentoEntity> entities);
    List<ConviteCasamentoEntity> toEntityList(List<Convite> domains);
}
