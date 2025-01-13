package org.slideshow.repository;

import org.slideshow.model.domain.SlideshowEntity;
import org.slideshow.model.projection.SlideshowDBProjection;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SlideshowRepository extends ReactiveCrudRepository<SlideshowEntity, Long> {

  //reactive repo doesn't support ORDER BY parametrization.
  // Hardcode added for same of simplicity
  //If it necessary to support ORDER BY value, then custom query with reactive databaseClient should be used instead
  @Query("""
              SELECT s.id AS slideshow_id,
                     i.id AS image_id, i.url, i.duration, i.added_at
              FROM slideshows s
                       LEFT JOIN images i
                                 ON i.id = ANY(s.images_ids::bigint[])
              WHERE s.id = :slideshowId
              ORDER BY i.added_at DESC
          """)
  Flux<SlideshowDBProjection> findSlideshowWithImagesById(Long slideshowId);

  @Modifying
  @Query("""
              UPDATE slideshows
              SET images_ids = array_remove(images_ids, :imageId)
              WHERE :imageId = ANY(images_ids)
          """)
  Mono<Integer> removeImageIdFromSlideshows(@Param("imageId") Long imageId);

}
