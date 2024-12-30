package org.slideshow.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "images")
public class ImageEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "image_id_seq")
  @SequenceGenerator(name = "image_id_seq", sequenceName = "image_id_seq")
  @Column
  private Long id;

  private String url;

  private Short duration;

  private LocalDateTime addedAt = LocalDateTime.now();

}
