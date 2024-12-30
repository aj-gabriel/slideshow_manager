package org.slideshow.service;

import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.SlideshowProjection;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SlideshowService {

  Mono<SlideshowProjection> createSlideshow(Flux<ImageEntity> imagesDTO);

}
