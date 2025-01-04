package org.slideshow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlideshowServiceFacade {

  private final ImageService imageService;
  private final SlideshowService slideshowService;

  @Transactional
  public Mono<SlideshowProjection> createSlideshow(Flux<ImageEntity> imagesDTO) {

    //filter list by empty id and create new images
    Mono<List<Long>> newImagesIds = imagesDTO
            .filter(dto -> dto.getId() == null)
            .as(imageService::createImages)
            .map(ImageEntity::getId)
            .collectList();

    //filter images with id
    Mono<List<Long>> existingImagesIds = imagesDTO
            .filter(dto -> dto.getId() != null)
            .map(ImageEntity::getId)
            .collectList();

    //combine them and create new Slideshow
    return Mono.from(Flux.merge(newImagesIds, existingImagesIds)
            .flatMap(ids -> slideshowService.createSlideshow(Mono.just(ids))));

  }

  public Mono<SlideshowProjection> getSlideshowById(Long id, Sort.Direction orderDirection) {
    return slideshowService.getSlideshowById(id, orderDirection);

  }

  public Mono<Void> deleteSlideshow(Mono<Long> slideshowId) {
    return slideshowService.deleteSlideshowById(slideshowId);
  }

  public Mono<Void> deleteImageAndUpdateSlideshow(Mono<Long> imageId) {
    return imageId
            .flatMap(imageService::deleteImageById)
            .thenReturn(imageId)
            .doOnSuccess(id -> log.info("Deleted image {}", id))
            .flatMap(slideshowService::removeImagesFromSlideshow)
            .doOnSuccess(count -> log.info("Total updated slideshows {}", count))
            .doOnError(e -> log.error("An error occurred while deleting image or updating slideshows", e))
            .onErrorResume(e -> Mono.empty())
            .then();
  }

}
