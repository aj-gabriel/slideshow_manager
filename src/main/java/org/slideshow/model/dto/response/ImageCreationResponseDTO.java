package org.slideshow.model.dto.response;

import java.util.List;

public record ImageCreationResponseDTO(Long id,
                                       String url,
                                       Short duration,
                                       List<ValidationErrorResponseDTO> validationErrors) {
}
