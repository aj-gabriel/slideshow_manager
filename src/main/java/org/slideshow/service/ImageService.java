package org.slideshow.service;

import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.ImageProjection;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageService {

  Mono<ImageEntity> createImage(Mono<ImageEntity> imageDTO);

  Flux<ImageEntity> createImages(Flux<ImageEntity> imagesDTO);

  Mono<ImageEntity> findImageById(Long id);

  Flux<ImageEntity> findImagesById(List<Long> ids);

  Flux<ImageProjection> findByKeywordAndDuration(String keyword, Integer duration, Sort.Direction orderDirection);

  Mono<Void> deleteImageById(Long id);

}
