package br.com.convite.mapper;

import br.com.convite.domain.Usuario;
import br.com.convite.gateway.persistence.entity.UsuarioEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    Usuario toDomain(UsuarioEntity entity);
    UsuarioEntity toEntity(Usuario domain);
}
