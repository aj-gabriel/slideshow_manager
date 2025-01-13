package org.slideshow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slideshow.model.domain.ProofOfPlayEventEntity;
import org.slideshow.repository.ProofOfPlayEventRepository;
import org.slideshow.service.ProofOfPlayEventService;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProofOfPlayEventServiceImplTest {

  @Mock
  private ProofOfPlayEventRepository eventRepository;

  @InjectMocks
  private ProofOfPlayEventServiceImpl proofOfPlayEventServiceImpl;

  private ProofOfPlayEventService proofOfPlayEventService;

  @BeforeEach
  void setUp() {
    proofOfPlayEventService = proofOfPlayEventServiceImpl;
  }

  @Test
  public void recordProofOfPlay() {
    //prepare
    ProofOfPlayEventEntity event = new ProofOfPlayEventEntity();
    event.setId(1L);
    when(eventRepository.save(any())).thenReturn(Mono.just(event));

    //execute
    StepVerifier.create(proofOfPlayEventService.recordProofOfPlay(Mono.just(event)))
            .expectNext(event)
            .verifyComplete();

    //verify
    verify(eventRepository).save(any(ProofOfPlayEventEntity.class));

  }
}