package org.slideshow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SlideshowServiceFacadeTest {

  @Mock
  private ImageService imageService;

  @Mock
  private SlideshowService slideshowService;

  @InjectMocks
  private SlideshowServiceFacade slideshowServiceFacade;


  @BeforeEach
  public void setUp() {
  }

  @Test
  @SuppressWarnings("unchecked")
  public void createSlideshow_ShouldCreateSlideshowWithNewAndExistingImages() {
    //prepare
    ImageEntity newImage = new ImageEntity();
    ImageEntity existingImage = new ImageEntity();
    existingImage.setId(1L);

    ImageEntity savedNewImage = new ImageEntity();
    savedNewImage.setId(2L);

    SlideshowEntity slideshowEntity = new SlideshowEntity();
    slideshowEntity.setId(3L);
    slideshowEntity.setImagesIds(List.of(savedNewImage.getId(), existingImage.getId()));

    SlideshowProjection slideshowProjection = mock(SlideshowProjection.class);

    when(imageService.createImages(any(Flux.class))).thenReturn(Flux.just(savedNewImage));
    when(slideshowService.createSlideshow(any(Mono.class))).thenReturn(Mono.just(slideshowEntity));
    when(slideshowService.getSlideshowById(any())).thenReturn(Mono.just(slideshowProjection));

    //execute
    StepVerifier.create(slideshowServiceFacade.createSlideshow(Flux.just(newImage, existingImage)))
            .expectNext(slideshowProjection)
            .verifyComplete();

    //validate
    verify(imageService).createImages(any(Flux.class));

    //count how many ids propagated to createSlideshow method
    verify(slideshowService).createSlideshow(argThat(ids -> {
      AtomicInteger count = new AtomicInteger();
      ids
              .subscribe(list -> count.set(list.size()))//subscribe to stream
              .dispose();//unsubscribe from stream
      return count.get() == 2;
    }));
  }

  @Test
  @SuppressWarnings("unchecked")
  void deleteImageAndUpdateSlideshow_ShouldDeleteAndUpdateSuccessfully() {
    //prepare
    when(imageService.deleteImageById(any())).thenReturn(Mono.empty());
    when(slideshowService.removeImagesFromSlideshow(any(Mono.class))).thenReturn(Mono.just(1));

    //execute
    StepVerifier.create(slideshowServiceFacade.deleteImageAndUpdateSlideshow(Mono.just(1L)))
            .verifyComplete();

    //validate
    verify(imageService).deleteImageById(any());
    verify(slideshowService).removeImagesFromSlideshow(any(Mono.class));
  }

  @Test
  void deleteImageAndUpdateSlideshow_ShouldHandleErrorGracefully() {
    //prepare
    when(imageService.deleteImageById(any())).thenReturn(Mono.error(new RuntimeException("Error deleting image")));

    //execute
    StepVerifier.create(slideshowServiceFacade.deleteImageAndUpdateSlideshow(Mono.just(1L)))
            .verifyComplete();

    //validate
    verify(imageService).deleteImageById(any());
    verifyNoInteractions(slideshowService);
  }
}