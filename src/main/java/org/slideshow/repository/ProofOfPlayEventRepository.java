package org.slideshow.repository;

import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.ProofOfPlayEventEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProofOfPlayEventRepository extends ReactiveCrudRepository<ProofOfPlayEventEntity, Long> {

}
