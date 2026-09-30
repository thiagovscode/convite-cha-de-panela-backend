package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.UsuarioEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends MongoRepository<UsuarioEntity, String> {
    Optional<UsuarioEntity> findByUsername(String username);
    Optional<UsuarioEntity> findByUsernameIgnoreCase(String username);
    List<UsuarioEntity> findAllByUsernameIgnoreCase(String username);
}
