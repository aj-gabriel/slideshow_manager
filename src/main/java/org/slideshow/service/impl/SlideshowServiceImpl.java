package org.slideshow.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.slideshow.repository.SlideshowRepository;
import org.slideshow.service.ImageService;
import org.slideshow.service.SlideshowService;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SlideshowServiceImpl implements SlideshowService {

  private final ImageService imageService;
  private final SlideshowRepository slideshowRepository;

  @Transactional
  public Mono<SlideshowProjection> createSlideshow(Flux<ImageEntity> imagesDTO) {

    //filter list by empty id and create new images
    Mono<List<ImageEntity>> createNewImages = imagesDTO
            .filter(dto -> dto.getId() == null)
            .as(imageService::createImages);

    //filter images with id
    Mono<List<ImageEntity>> getExistingImages = imagesDTO
            .filter(dto -> dto.getId() != null)
            .collectList();

    //combine them and extract images ids
    Mono<List<Long>> imageIds = Flux
            .merge(createNewImages, getExistingImages)
            .flatMap(Flux::fromIterable)
            .map(ImageEntity::getId)
            .collect(Collectors.toList());

    return imageIds.flatMap(ids -> {
              SlideshowEntity slideshowEntity = new SlideshowEntity();
              slideshowEntity.setImagesIds(ids);
              return slideshowRepository.save(slideshowEntity);
            })
            //retrieve from DB slideshow with images as projection due to reactive repositories mapping specific
            .flatMap(savedSlideshow ->
                    slideshowRepository.findSlideshowWithImagesById(savedSlideshow.getId(), "ASC")
            );

  }

}
