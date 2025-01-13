package org.slideshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ProofOfPlayEventEntity;
import org.slideshow.repository.ProofOfPlayEventRepository;
import org.slideshow.service.ProofOfPlayEventService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProofOfPlayEventServiceImpl implements ProofOfPlayEventService {

  private final ProofOfPlayEventRepository eventRepository;

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<ProofOfPlayEventEntity> recordProofOfPlay(Mono<ProofOfPlayEventEntity> event) {
    return event.flatMap(eventRepository::save);
  }

}
