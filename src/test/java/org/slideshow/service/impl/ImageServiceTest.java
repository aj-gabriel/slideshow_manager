package org.slideshow.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivestreams.Publisher;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.ImageProjection;
import org.slideshow.repository.ImageRepository;
import org.slideshow.service.ImageService;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {

  @Mock
  private ImageRepository imageRepository;

  @InjectMocks
  private ImageServiceImpl imageServiceImpl;

  private ImageService imageService;

  private ImageEntity imageEntity;

  @BeforeEach
  void setUp() {
    imageService = imageServiceImpl;
    imageEntity = new ImageEntity();
    imageEntity.setId(1L);
  }

  @Test
  void createImage_ShouldSaveImage() {
    //prepare
    when(imageRepository.save(any())).thenReturn(Mono.just(imageEntity));

    //execute
    StepVerifier.create(imageService.createImage(Mono.just(imageEntity)))
            .expectNext(imageEntity)
            .verifyComplete();

    //verify
    verify(imageRepository).save(any(ImageEntity.class));
  }

  @Test
  @SuppressWarnings("unchecked")
  void createImages_ShouldSaveMultipleImages() {
    //prepare
    List<ImageEntity> imageEntities = Arrays.asList(new ImageEntity(), new ImageEntity());
    when(imageRepository.saveAll(any(Publisher.class))).thenReturn(Flux.fromIterable(imageEntities));

    //execute
    StepVerifier.create(imageService.createImages(Flux.fromIterable(imageEntities)))
            .expectNextCount(2)
            .verifyComplete();

    //validate
    verify(imageRepository).saveAll(any(Publisher.class));
  }

  @Test
  void findImageById_ShouldReturnImage() {
    //prepare
    when(imageRepository.findById(1L)).thenReturn(Mono.just(imageEntity));

    //execute
    StepVerifier.create(imageService.findImageById(1L))
            .expectNext(imageEntity)
            .verifyComplete();

    //verify
    verify(imageRepository).findById(1L);
  }

  @Test
  void findImagesById_ShouldReturnMultipleImages() {
    //prepare
    List<Long> ids = Arrays.asList(1L, 2L);
    when(imageRepository.findAllById(ids)).thenReturn(Flux.fromIterable(Arrays.asList(imageEntity, imageEntity)));

    //execute
    StepVerifier.create(imageService.findImagesById(ids))
            .expectNextCount(2)
            .verifyComplete();

    //verify
    verify(imageRepository).findAllById(ids);
  }

  @Test
  void findByKeywordAndDuration_ShouldReturnMatchingImages() {
    //prepare
    ImageProjection projection = mock(ImageProjection.class);
    when(imageRepository.findByKeywordAndDuration("test", 10, "ASC"))
            .thenReturn(Flux.just(projection));

    //execute
    StepVerifier.create(imageService.findByKeywordAndDuration("test", 10, Sort.Direction.ASC))
            .expectNext(projection)
            .verifyComplete();

    //verify
    verify(imageRepository).findByKeywordAndDuration("test", 10, "ASC");
  }

  @Test
  void deleteImageById_ShouldDeleteImage() {
    //prepare
    when(imageRepository.deleteById(1L)).thenReturn(Mono.empty());

    //execute
    StepVerifier.create(imageService.deleteImageById(1L))
            .verifyComplete();

    //verify
    verify(imageRepository).deleteById(1L);
  }
}