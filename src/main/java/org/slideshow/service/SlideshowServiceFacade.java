package org.slideshow.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SlideshowServiceFacade {

  private final ImageService imageService;
  private final SlideshowService slideshowService;

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<SlideshowProjection> createSlideshow(Flux<ImageEntity> imagesDTO) {
    //combine them and create new Slideshow
    return imagesDTO
            .filter(dto -> dto.getId() == null)
            .as(imageService::createImages)
            .map(ImageEntity::getId)
            .collectList()
            .flatMap(newImagesIds -> imagesDTO
                    .filter(dto -> dto.getId() != null)
                    .map(ImageEntity::getId)
                    .collectList()
                    .map(existingImagesIds -> {
                      newImagesIds.addAll(existingImagesIds);
                      return newImagesIds;
                    })
            )
            .flatMap(ids -> slideshowService.createSlideshow(Mono.just(ids)))
            //retrieve from DB slideshow with images as projection due to reactive repositories mapping specific
            .flatMap(savedSlideshow -> slideshowService.getSlideshowById(savedSlideshow.getId()));

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
