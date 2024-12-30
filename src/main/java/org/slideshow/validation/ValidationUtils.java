package org.slideshow.validation;

import org.slideshow.model.dto.request.ImageDetailsRequestDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

public class ValidationUtils {

  public static Flux<ImageDetailsRequestDTO> filterValidImages(Flux<ImageDetailsRequestDTO> images, Flux<ValidationError> validationErrors) {
    return images.flatMap(image ->
            validationErrors
                    .filter(error -> isErrorRelatedToImage(image, error))
                    .hasElements()
                    .flatMap(hasErrors -> hasErrors ? Mono.empty() : Mono.just(image))
    );
  }

  private static boolean isErrorRelatedToImage(ImageDetailsRequestDTO image, ValidationError error) {
    Map<String, Object> details = error.exchangeContext();
    int imageHash = image.hashCode();

    return details.containsValue(imageHash) ||
            (image.id() != null && details.containsValue(image.id())) ||
            (image.url() != null && details.containsValue(image.url()));
  }

}
