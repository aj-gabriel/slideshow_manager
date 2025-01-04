package org.slideshow.repository;

import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowProjection;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface SlideshowRepository extends ReactiveCrudRepository<SlideshowEntity, Long> {

  @Query("""
        SELECT s.* AS slideshow_id, i.* 
        FROM slideshows s
        LEFT JOIN images i ON i.id = ANY(s.images)
        WHERE s.id = :slideshowId
        ORDER BY i.added_at :orderDirection 
    """)
  Mono<SlideshowProjection> findSlideshowWithImagesById(Long slideshowId, String orderDirection);

  @Modifying
  @Query("UPDATE slideshows SET images = array_remove(images, :imageId) WHERE :imageId = ANY(images)")
  Mono<Integer> removeImageIdFromSlideshows(@Param("imageId") Long imageId);

}
