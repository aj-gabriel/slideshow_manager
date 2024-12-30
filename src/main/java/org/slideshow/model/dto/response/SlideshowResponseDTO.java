package org.slideshow.model.dto.response;

import java.util.List;

public record SlideshowResponseDTO(
        Long id,
        List<ImageResponseDTO> images,
        List<ValidationErrorResponseDTO> validationErrors) {
}
