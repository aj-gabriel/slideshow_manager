package org.slideshow.service;

import org.slideshow.model.domain.ImageEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ImageService {

  Mono<ImageEntity> createImage(Mono<ImageEntity> imageDTO);

  Mono<List<ImageEntity>> createImages(Flux<ImageEntity> imagesDTO);

  Mono<ImageEntity> findImageById(Long id);

  Flux<ImageEntity> findImagesById(List<Long> ids);

}
