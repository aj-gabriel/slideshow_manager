package org.slideshow.model.dto.response;

public record ValidationErrorResponseDTO(
        String code,
        String message) {
}
