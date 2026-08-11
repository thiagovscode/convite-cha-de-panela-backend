package br.com.convite.mapper;

import br.com.convite.domain.Rsvp;
import br.com.convite.gateway.persistence.entity.RsvpEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RsvpMapper {
    Rsvp toDomain(RsvpEntity entity);
    RsvpEntity toEntity(Rsvp domain);
}
