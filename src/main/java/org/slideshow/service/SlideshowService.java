package org.slideshow.service;

import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SlideshowService {

  Mono<SlideshowEntity> createSlideshow(Mono<List<Long>> imageIds);

  Mono<SlideshowProjection> getSlideshowById(Long id);

  Mono<Void> deleteSlideshowById(Mono<Long> id);

  Mono<Integer> removeImagesFromSlideshow(Mono<Long> imageId);

}
