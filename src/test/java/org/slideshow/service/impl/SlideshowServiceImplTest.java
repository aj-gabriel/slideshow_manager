package org.slideshow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowDBProjection;
import org.slideshow.model.projection.SlideshowProjection;
import org.slideshow.repository.SlideshowRepository;
import org.slideshow.service.SlideshowService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SlideshowServiceImplTest {

  @Mock
  private SlideshowRepository slideshowRepository;

  @InjectMocks
  private SlideshowServiceImpl slideshowServiceImpl;

  private SlideshowService slideshowService;

  private SlideshowEntity slideshowEntity;

  private SlideshowDBProjection slideshowDBProjection;
  private SlideshowProjection slideshowProjection;

  @BeforeEach
  public void setUp() {
    slideshowService = slideshowServiceImpl;

    slideshowEntity = new SlideshowEntity();
    slideshowEntity.setId(1L);
    slideshowEntity.setImagesIds(Arrays.asList(1L, 2L));
    short duration = 10;
    String url = "test_url";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setUrl(url);
    imageEntity.setDuration(duration);

    slideshowDBProjection = new SlideshowDBProjection(slideshowEntity.getId(), 1L, url, duration, LocalDateTime.now());
    slideshowProjection = new SlideshowProjection(slideshowEntity.getId(), List.of(imageEntity));

//    slideshowProjection = mock(SlideshowProjection.class);

  }

  @Test
  public void createSlideshow_ShouldSaveSlideshowAndReturnProjection() {
    //prepare
    List<Long> imageIds = Arrays.asList(1L, 2L);

    when(slideshowRepository.save(any(SlideshowEntity.class)))
            .thenReturn(Mono.just(slideshowEntity));

    //execute
    StepVerifier.create(slideshowService.createSlideshow(Mono.just(imageIds)))
            .expectNext(slideshowEntity)
            .verifyComplete();

    //verify
    verify(slideshowRepository).save(any(SlideshowEntity.class));
  }

  @Test
  public void getSlideshowById_ShouldReturnProjection() {
    //prepare
    when(slideshowRepository.findSlideshowWithImagesById(1L))
            .thenReturn(Flux.just(slideshowDBProjection));

    //execute
    StepVerifier.create(slideshowService.getSlideshowById(1L))
            .expectNextMatches(actual ->
                    actual.slideshowId().equals(slideshowProjection.slideshowId()) &&
                            actual.images().size() == slideshowProjection.images().size() &&
                            actual.images().get(0).getUrl().equals(slideshowProjection.images().get(0).getUrl())
            )
            .verifyComplete();

    //verify
    verify(slideshowRepository).findSlideshowWithImagesById(1L);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void deleteSlideshowById_ShouldDeleteSlideshow() {
    //prepare
    when(slideshowRepository.deleteById(any(Publisher.class))).thenReturn(Mono.empty());

    //execute
    StepVerifier.create(slideshowService.deleteSlideshowById(Mono.just(1L)))
            .verifyComplete();

    //verify
    verify(slideshowRepository).deleteById(any(Publisher.class));
  }

  @Test
  public void removeImagesFromSlideshow_ShouldRemoveImageAndReturnCount() {
    //prepare
    when(slideshowRepository.removeImageIdFromSlideshows(1L)).thenReturn(Mono.just(1));

    //execute
    StepVerifier.create(slideshowService.removeImagesFromSlideshow(Mono.just(1L)))
            .expectNext(1)
            .verifyComplete();

    //verify
    verify(slideshowRepository).removeImageIdFromSlideshows(1L);
  }
}