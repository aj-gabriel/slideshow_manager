package org.slideshow.repository;

import org.slideshow.model.domain.ImageEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ImageRepository extends ReactiveCrudRepository<ImageEntity, Long> {

}
