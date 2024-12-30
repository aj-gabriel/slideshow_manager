package org.slideshow.repository;

import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SlideshowRepository extends ReactiveCrudRepository<SlideshowEntity, Long> {

  @Query("""
        SELECT s.* AS slideshow_id, i.* 
        FROM slideshows s
        LEFT JOIN images i ON i.id = ANY(s.images)
        WHERE s.id = :slideshowId
        ORDER BY i.added_at :orderDirection 
    """)
  Mono<SlideshowProjection> findSlideshowWithImagesById(Long slideshowId, String orderDirection);

}
