package org.slideshow.validation;

import lombok.Getter;

@Getter
public enum ValidationErrorCodes {
  INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR", "Something went wrong."),
  INTERNAL_VALIDATION_ERROR("INTERNAL_VALIDATION_ERROR", "Unexpected error: "),
  EMPTY_LIST("EMPTY_LIST", "Images list cannot be empty"),
  INVALID_IMAGE("INVALID_IMAGE", "Image cannot be null"),
  INVALID_IMAGE_INSTANCE_ID("INVALID_IMAGE_INSTANCE_ID", "Image with specified ID {id} not found."),
  INVALID_IMAGE_URL("INVALID_IMAGE_URL", "URL must be provided, and URL must not exceed 255 characters"),
  INVALID_IMAGE_URL_LENGTH("INVALID_IMAGE_URL_LENGTH", "URL must not exceed 255 characters. Current length {length}"),
  INVALID_IMAGE_DURATION("INVALID_IMAGE_DURATION", "Duration must be between 1 and 300 seconds"),
  NO_VALID_IMAGES("NO_VALID_IMAGES", "At least one valid image must be provided");

  private final String code;
  private final String defaultMessage;

  ValidationErrorCodes(String code, String defaultMessage) {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

}
