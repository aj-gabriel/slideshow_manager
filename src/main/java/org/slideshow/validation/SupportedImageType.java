package org.slideshow.validation;

import lombok.Getter;

@Getter
public enum SupportedImageType {

  JPEG("image/jpeg"),
  PNG("image/png"),
  GIF("image/gif"),
  WEBP("image/webp"),
  SVG("image/svg+xml"),
  TIFF("image/tiff"),
  BMP("image/bmp"),
  ICO("image/x-icon"),
  AVIF("image/avif"),
  HEIC("image/heic"),
  HEIF("image/heif"),
  JP2("image/jp2");

  private final String contentType;

  SupportedImageType(String contentType) {
    this.contentType = contentType;
  }

  public static boolean isTypeSupported(String contentType) {
    for (SupportedImageType type : values()) {
      if (type.getContentType().equals(contentType)) {
        return true;
      }
    }
    return false;
  }

}
