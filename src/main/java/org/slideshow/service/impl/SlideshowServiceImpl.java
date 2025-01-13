package org.slideshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.slideshow.repository.SlideshowRepository;
import org.slideshow.service.SlideshowService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlideshowServiceImpl implements SlideshowService {

  private final SlideshowRepository slideshowRepository;

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<SlideshowProjection> createSlideshow(Mono<List<Long>> imageIds) {
    return imageIds.flatMap(ids -> {
              SlideshowEntity slideshowEntity = new SlideshowEntity();
              slideshowEntity.setImagesIds(ids);
              return slideshowRepository.save(slideshowEntity);
            })
            //retrieve from DB slideshow with images as projection due to reactive repositories mapping specific
            .flatMap(savedSlideshow -> getSlideshowById(savedSlideshow.getId(), Sort.Direction.ASC));
  }

  public Mono<SlideshowProjection> getSlideshowById(Long id, Sort.Direction orderDirection) {
    return slideshowRepository.findSlideshowWithImagesById(id, orderDirection.name());
  }

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<Void> deleteSlideshowById(Mono<Long> id) {
    return slideshowRepository.deleteById(id);
  }

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<Integer> removeImagesFromSlideshow(Mono<Long> imageId) {
    return imageId.flatMap(slideshowRepository::removeImageIdFromSlideshows);
  }

}
