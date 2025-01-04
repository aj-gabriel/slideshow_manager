package org.slideshow.repository;

import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.projection.ImageProjection;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ImageRepository extends ReactiveCrudRepository<ImageEntity, Long> {

  // The 'english' configuration in to_tsvector is a hardcoded implementation for full-text search.
  // It can be extended to support multiple languages by modifying the ImageEntity structure and
  // adding a 'language' column if such business requirements arise in the future.
  @Query("""
              SELECT * FROM images 
              WHERE (:keyword IS NULL OR to_tsvector('english', url) @@ plainto_tsquery(:keyword))
                     AND (:duration IS NULL OR duration = :duration)
              ORDER BY added_at :direction
          """)
  Flux<ImageProjection> findByKeywordAndDuration(
          @Param("keyword") String keyword,
          @Param("duration") Integer duration,
          @Param("direction") String direction);

}
