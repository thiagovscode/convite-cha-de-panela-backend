package br.com.convite.gateway.persistence;

import br.com.convite.gateway.persistence.entity.RsvpEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface RsvpRepository extends MongoRepository<RsvpEntity, String> {
}
