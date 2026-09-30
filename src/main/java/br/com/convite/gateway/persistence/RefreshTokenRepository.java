package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.RefreshTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends MongoRepository<RefreshTokenEntity, String> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    List<RefreshTokenEntity> findAllByUsername(String username);

    List<RefreshTokenEntity> findAllByFamilyId(String familyId);

    void deleteAllByUsername(String username);

    void deleteAllByExpiresAtBefore(Instant data);
}
