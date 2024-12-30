package org.slideshow.model.projection;

import org.slideshow.model.domain.ImageEntity;

import java.util.List;

public record SlideshowProjection(Long slideshowId,
                                  List<ImageEntity> images) {
}
