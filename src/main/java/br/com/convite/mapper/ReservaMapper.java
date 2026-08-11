package br.com.convite.mapper;

import br.com.convite.domain.Reserva;
import br.com.convite.gateway.persistence.entity.ReservaEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReservaMapper {
    Reserva toDomain(ReservaEntity entity);
    ReservaEntity toEntity(Reserva domain);
}
