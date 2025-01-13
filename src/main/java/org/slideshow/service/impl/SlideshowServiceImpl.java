package org.slideshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.slideshow.repository.SlideshowRepository;
import org.slideshow.service.SlideshowService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlideshowServiceImpl implements SlideshowService {

  private final SlideshowRepository slideshowRepository;

  @Transactional(transactionManager = "reactiveTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public Mono<SlideshowEntity> createSlideshow(Mono<List<Long>> imageIds) {
    return imageIds.flatMap(ids -> {
      SlideshowEntity slideshowEntity = new SlideshowEntity();
      slideshowEntity.setImagesIds(ids);
      return slideshowRepository.save(slideshowEntity);
    });
  }

  public Mono<SlideshowProjection> getSlideshowById(Long id) {
    return slideshowRepository.findSlideshowWithImagesById(id)
            .collectList()
            .map(rows -> {
              List<ImageEntity> images = rows.stream()
                      .map(r -> new ImageEntity(
                              r.imageId(),
                              r.url(),
                              r.duration(),
                              r.addedAt()
                      ))
                      .collect(Collectors.toList());
              return new SlideshowProjection(id, images);
            });
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
