package org.slideshow.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.dto.request.ImageCreateRequestDTO;
import org.slideshow.model.dto.request.SlideshowRequestDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.validation.validators.ImageValidationService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_VALIDATION_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImagesValidationFacade {

  private final ImageValidationService validator;

  public Mono<ValidationErrorResponseDTO> validateRequest(ImageCreateRequestDTO r) {
    return validator.validateUrl(Mono.just(r.url()))
            .flatMap(error -> Mono.just(new ValidationErrorResponseDTO(error.code(), error.message()))
            )
            .switchIfEmpty(validator.validateImageContent(Mono.just(r.url()))
                    .flatMap(error -> Mono.just(new ValidationErrorResponseDTO(error.code(), error.message())))
            )
            .switchIfEmpty(validator.validateDuration(Mono.just(r.duration()))
                    .flatMap(error -> Mono.just(new ValidationErrorResponseDTO(error.code(), error.message())))
            )
            .switchIfEmpty(Mono.empty());
  }


  public Mono<List<ValidationError>> validateImages(Mono<SlideshowRequestDTO> request) {
    if (request == null) {
      return Mono.just(
              List.of(new ValidationError(
                      ValidationErrorCodes.EMPTY_LIST.getCode(),
                      ValidationErrorCodes.EMPTY_LIST.getDefaultMessage(),
                      new HashMap<>())
              )
      );
    }

    Mono<List<ValidationError>> imagesExistenceErrors = validator.validateImagesExistence(request);

    Mono<List<ValidationError>> imagesIntegrityErrors = request
            .flatMapMany(r -> Flux.fromIterable(r.images()))
            .flatMap(image -> validator.validateImageIntegrity(Mono.just(image)))
            .collectList();


    return collectErrors(imagesExistenceErrors, imagesIntegrityErrors);
  }

  private Mono<List<ValidationError>> collectErrors(Mono<List<ValidationError>> imagesExistenceErrors,
                                                    Mono<List<ValidationError>> imagesIntegrityErrors) {
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

}
