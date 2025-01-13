package org.slideshow.model.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Getter
@Setter
@Table(name = "images")
@NoArgsConstructor
@AllArgsConstructor
public class ImageEntity {

  @Id
  private Long id;

  private String url;

  private Short duration;

  private LocalDateTime addedAt = LocalDateTime.now();

}
