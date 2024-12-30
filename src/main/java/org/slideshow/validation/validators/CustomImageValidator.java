package org.slideshow.validation.validators;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.dto.request.ImageDetailsRequestDTO;
import org.slideshow.model.dto.request.SlideshowRequestDTO;
import org.slideshow.service.ImageService;
import org.slideshow.validation.ValidationError;
import org.slideshow.validation.ValidationErrorCodes;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;

import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_VALIDATION_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomImageValidator {

  public static final String IMAGE_ID_KEY = "id";
  public static final String IMAGE_HASH_KEY = "hash";

  public static final String IMAGE_ID_PLACEHOLDER = "{id}";
  public static final String IMAGE_LENGTH_PLACEHOLDER = "{length}";
  public static final String INVALID_VALUE_KEY = "invalidValue";

  private final ImageService imageService;

  public Mono<List<ValidationError>> validate(Mono<SlideshowRequestDTO> request) {
    if (request == null) {
      return Mono.just(
              List.of(new ValidationError(
                      ValidationErrorCodes.EMPTY_LIST.getCode(),
                      ValidationErrorCodes.EMPTY_LIST.getDefaultMessage(),
                      new HashMap<>())
              )
      );
    }

    Mono<List<ValidationError>> imagesExistenceErrors = validateImagesExistence(request);

    Mono<List<ValidationError>> imagesIntegrityErrors = request
            .flatMapMany(r -> Flux.fromIterable(r.images()))
            .flatMap(this::validateImageIntegrity)
            .collectList();


    return Mono.zip(imagesExistenceErrors, imagesIntegrityErrors)
            .map(tuple -> {
              List<ValidationError> combinedErrors = new ArrayList<>();
              combinedErrors.addAll(tuple.getT1());
              combinedErrors.addAll(tuple.getT2());
              return combinedErrors;
            })
            .switchIfEmpty(Mono.just(List.of()))
            .onErrorResume(e -> {
              log.error("Error on validating images: {}", e.getMessage());

              return Mono.just(List.of(new ValidationError(
                      INTERNAL_VALIDATION_ERROR.getCode(),
                      INTERNAL_VALIDATION_ERROR.getDefaultMessage() + e.getMessage(),
                      Map.of("exception", e.getClass().getSimpleName()))
              ));
            });
  }


  public Flux<ValidationError> validateImageIntegrity(ImageDetailsRequestDTO image) {
    if (image == null) {
      return Flux.just(new ValidationError(
              ValidationErrorCodes.INVALID_IMAGE.getCode(),
              ValidationErrorCodes.INVALID_IMAGE.getDefaultMessage(), new HashMap<>())
      );
    }

    List<ValidationError> errors = new ArrayList<>();

    if (image.id() == null) {
      if (image.url() == null || image.url().isBlank()) {
        errors.add(new ValidationError(
                        ValidationErrorCodes.INVALID_IMAGE_URL.getCode(),
                        ValidationErrorCodes.INVALID_IMAGE_URL.getDefaultMessage(),
                        Map.of(IMAGE_HASH_KEY, image.hashCode())
                )
        );
      } else if (image.url().length() > 255) {
        errors.add(new ValidationError(
                        ValidationErrorCodes.INVALID_IMAGE_URL_LENGTH.getCode(),
                        ValidationErrorCodes.INVALID_IMAGE_URL_LENGTH.getDefaultMessage()
                                .replace(IMAGE_LENGTH_PLACEHOLDER, String.valueOf(image.url().length())),
                        Map.of(IMAGE_HASH_KEY, image.hashCode(), INVALID_VALUE_KEY, String.valueOf(image.url().length()))
                )
        );
      }
    }

    if (image.duration() == null || image.duration() < 1 || image.duration() > 300) {
      errors.add(new ValidationError(
                      ValidationErrorCodes.INVALID_IMAGE_DURATION.getCode(),
                      ValidationErrorCodes.INVALID_IMAGE_DURATION.getDefaultMessage(),
                      Map.of(IMAGE_HASH_KEY, image.hashCode(), INVALID_VALUE_KEY, String.valueOf(image.duration()))
              )
      );
    }

    return Flux.fromIterable(errors);
  }

  /**
   * Method checks if specified image ids are present in DB
   *
   * @param imageIds ids for check
   * @return ValidationError list or empty
   */
  public Mono<List<ValidationError>> validateImageExistenceById(List<Long> imageIds) {
    return Flux.fromIterable(imageIds)
            .filter(Objects::nonNull)
            .distinct()// Remove possible duplicates
            .collectList()
            .flatMapMany(imageService::findImagesById)
            .map(ImageEntity::getId)
            .collectList()
            .map(foundIds -> {
              List<Long> missingIds = imageIds
                      .stream()
                      .filter(id -> !foundIds.contains(id))
                      .toList();

              return missingIds.stream()
                      .map(missingId ->
                              new ValidationError(
                                      ValidationErrorCodes.INVALID_IMAGE_INSTANCE_ID.getCode(),
                                      ValidationErrorCodes.INVALID_IMAGE_INSTANCE_ID.getDefaultMessage()
                                              .replace(IMAGE_ID_PLACEHOLDER, String.valueOf(missingId)),
                                      Map.of(IMAGE_ID_KEY, missingId)
                              )
                      )
                      .toList();


            })
            .switchIfEmpty(Mono.just(List.of()))
            .onErrorResume(e -> {
              log.error("Error on retrieving images: {}", e.getMessage());
              return Mono.just(List.of(new ValidationError(
                      INTERNAL_VALIDATION_ERROR.getCode(),
                      INTERNAL_VALIDATION_ERROR.getDefaultMessage() + e.getMessage(),
                      Map.of("exception", e.getClass().getSimpleName()))
              ));
            });
  }

  /**
   * Act same as CustomImageValidator#validateImageExistenceById(java.util.List),
   * but extract image ids from SlideshowRequestDTO
   *
   * @param request SlideshowRequestDTO as payload
   * @return ValidationError list or empty
   */
  private Mono<List<ValidationError>> validateImagesExistence(Mono<SlideshowRequestDTO> request) {

    return request
            .flatMap(r -> {
              List<Long> imageIds = r.images()
                      .stream()
                      .map(ImageDetailsRequestDTO::id)
                      .filter(Objects::nonNull)
                      .distinct() // Remove possible duplicates
                      .toList();

              return validateImageExistenceById(imageIds);
            })
            .switchIfEmpty(Mono.just(List.of()));
  }

}