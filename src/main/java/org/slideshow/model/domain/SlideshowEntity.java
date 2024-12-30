package org.slideshow.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "slideshows")
public class SlideshowEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "slideshow_id_seq")
  @SequenceGenerator(name = "slideshow_id_seq", sequenceName = "slideshow_id_seq")
  @Column
  private Long id;

  //store images ids instead of many-to-many relationships
  //List used for duplicated images
  @ElementCollection
  private List<Long> imagesIds = new ArrayList<>();

  private LocalDateTime createdAt = LocalDateTime.now();

}
