package org.slideshow.model.dto.request;

import java.time.OffsetDateTime;

//stored content maybe extended, depends on business-demands
//e.g.
//deviceType, deviceModel, os, browser, screenResolution
//location: geo-data, city
//tags, etc
public record ProofOfPlayEventDTO(Long userId,
                                  OffsetDateTime displayedAt,
                                  OffsetDateTime replacedAt,
                                  Short actualDuration) {
}
