package org.slideshow.model.projection;

import java.time.LocalDateTime;

public record SlideshowDBProjection(Long slideshowId,
                                    Long imageId,
                                    String url,
                                    Short duration,
                                    LocalDateTime addedAt) {
}
