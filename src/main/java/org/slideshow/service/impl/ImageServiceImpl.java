package org.slideshow.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.ImageProjection;
import org.slideshow.repository.ImageRepository;
import org.slideshow.service.ImageService;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ImageRepository imageRepository;

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<ImageEntity> createImage(Mono<ImageEntity> imageDTO) {
    return imageDTO.flatMap(imageRepository::save);
  }

  @Transactional(transactionManager = "reactiveTransactionManager", propagation = Propagation.REQUIRES_NEW)
  public Flux<ImageEntity> createImages(Flux<ImageEntity> imagesDTO) {
    return imagesDTO.as(imageRepository::saveAll);
  }

  public Mono<ImageEntity> findImageById(Long id) {
    return imageRepository.findById(id);
  }

  public Flux<ImageEntity> findImagesById(List<Long> ids) {
    return imageRepository.findAllById(ids);
  }

  @Override
  public Flux<ImageProjection> findByKeywordAndDuration(String keyword, Integer duration, Sort.Direction orderDirection) {
    return imageRepository.findByKeywordAndDuration(keyword, duration, orderDirection.name());
  }

  @Transactional(transactionManager = "reactiveTransactionManager")
  public Mono<Void> deleteImageById(Long id) {
    return imageRepository.deleteById(id);
  }

}
