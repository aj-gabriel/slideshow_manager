package org.slideshow.validation.validators;

import lombok.extern.slf4j.Slf4j;
import org.slideshow.validation.SupportedImageType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class ImageContentTypeValidator {

  private final WebClient webClient;

  public ImageContentTypeValidator(WebClient.Builder webClientBuilder) {
    this.webClient = webClientBuilder.baseUrl("").build();
  }

  public Mono<Boolean> validateImageUrl(String imageUrl) {
    return webClient.head()//send head request to retrieve just lightweight headers
            .uri(imageUrl)
            .exchangeToMono(response -> {
              String contentType = response.headers().contentType().map(Object::toString).orElse(null);
              return Mono.just(contentType != null && isSupported(contentType));
            })
            .onErrorResume(e -> Mono.just(false));
  }

  // This method should validate image content type.
  // It is separated to allow for future extension, such as additional validation checks for:
  // - File size (e.g., ensuring the image is below a specific size limit).
  // - Dimensions (e.g., checking the image's width and height meet certain criteria).
  // - Additional content properties (e.g., ensuring the image is not corrupted).
  // The current implementation checks only if the content type is supported.
  private static boolean isSupported(String contentType) {
    return SupportedImageType.isTypeSupported(contentType);
  }

}
