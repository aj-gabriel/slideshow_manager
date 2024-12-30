package org.slideshow.model.dto.request;

public record ImageDetailsRequestDTO(
        Long id,
        String url,
        Short duration
) {
}
