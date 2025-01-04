package org.slideshow.model.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Table(name = "slideshows")
public class SlideshowEntity {

  @Id
  @Column
  private Long id;

  //store images ids instead of many-to-many relationships
  //List used for duplicated images
  private List<Long> imagesIds = new ArrayList<>();

  private LocalDateTime createdAt = LocalDateTime.now();

}
