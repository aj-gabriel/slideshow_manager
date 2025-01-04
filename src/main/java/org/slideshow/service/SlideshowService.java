package org.slideshow.service;

import org.slideshow.model.projection.SlideshowProjection;
import org.springframework.data.domain.Sort;
import reactor.core.publisher.Mono;

import java.util.List;

public interface SlideshowService {

  Mono<SlideshowProjection> createSlideshow(Mono<List<Long>> imageIds);

  Mono<SlideshowProjection> getSlideshowById(Long id, Sort.Direction orderDirection);

  Mono<Void> deleteSlideshowById(Mono<Long> id);

  Mono<Integer> removeImagesFromSlideshow(Mono<Long> imageId);

}
