package org.slideshow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.SlideshowProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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

  private ImageEntity imageEntity;

  private SlideshowProjection slideshowProjection;

  @BeforeEach
  public void setUp() {
    imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    slideshowProjection = mock(SlideshowProjection.class);
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

    when(imageService.createImages(any(Flux.class))).thenReturn(Flux.just(savedNewImage));
    when(slideshowService.createSlideshow(any(Mono.class))).thenReturn(Mono.just(slideshowProjection));

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