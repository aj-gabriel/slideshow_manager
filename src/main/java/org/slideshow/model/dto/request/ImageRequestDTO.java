package org.slideshow.model.dto.request;

import jakarta.validation.constraints.*;

public record ImageRequestDTO(
        @NotBlank(message = "URL cannot be blank")
        @Size(max = 255, message = "URL length must not exceed 255 characters")
        String url,

        @NotNull(message = "Duration cannot be null")
        @Min(value = 1, message = "Duration must be at least 1 second")
        @Max(value = 300, message = "Duration must not exceed 300 seconds")
        Short duration) {
}
