package org.slideshow.validation;

import lombok.Getter;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;

@Getter
public class ImageValidationException extends RuntimeException {
  private final ValidationErrorResponseDTO error;

  public ImageValidationException(ValidationErrorResponseDTO error) {
    this.error = error;
  }

}
