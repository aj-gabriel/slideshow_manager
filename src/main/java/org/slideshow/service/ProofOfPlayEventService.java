package org.slideshow.service;

import org.slideshow.model.domain.ProofOfPlayEventEntity;
import reactor.core.publisher.Mono;

public interface ProofOfPlayEventService {
  Mono<ProofOfPlayEventEntity> recordProofOfPlay(Mono<ProofOfPlayEventEntity> event);
}
