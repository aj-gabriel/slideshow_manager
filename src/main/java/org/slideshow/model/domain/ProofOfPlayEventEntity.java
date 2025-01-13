package org.slideshow.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table("proof_of_play_events")
public class ProofOfPlayEventEntity {

  @Id
  private Long id;

  private Long imageId;

  private Long slideshowId;

  private Long userId;

  private OffsetDateTime replacedAt;

  private OffsetDateTime displayedAt;

  private Short actualDuration;
}
