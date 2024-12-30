package org.slideshow.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.repository.ImageRepository;
import org.slideshow.service.ImageService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

  private final ObjectMapper objectMapper;
  private final ImageRepository imageRepository;

  @Transactional
  public Mono<ImageEntity> createImage(Mono<ImageEntity> imageDTO) {
    return imageDTO.flatMap(imageRepository::save);
  }

  @Transactional
  public Mono<List<ImageEntity>> createImages(Flux<ImageEntity> imagesDTO) {
    return imagesDTO
            .as(imageRepository::saveAll)
            .collectList();

  }

  public Mono<ImageEntity> findImageById(Long id) {
    return imageRepository.findById(id);
  }

  public Flux<ImageEntity> findImagesById(List<Long> ids) {
    return imageRepository.findAllById(ids);
  }

}
