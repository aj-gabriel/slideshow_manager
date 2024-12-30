package org.slideshow.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.dto.request.ImageDetailsRequestDTO;
import org.slideshow.model.dto.request.SlideshowRequestDTO;
import org.slideshow.model.dto.response.SlideshowResponseDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.service.SlideshowService;
import org.slideshow.validation.ValidationError;
import org.slideshow.validation.validators.CustomImageValidator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_SERVER_ERROR;
import static org.slideshow.validation.ValidationUtils.filterValidImages;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/slideshows")
public class SlideshowController {

  private final ObjectMapper objectMapper;
  private final CustomImageValidator validator;
  private final SlideshowService slideshowService;


  /**
   * One of the possible implementations, depends on contract and business logic.
   * May produce exception and then handle it to wrap in corresponding response.
   * Idea below - in case if there is no at least one valid image,
   * then just return 400 Bad request and response with empty id + validation errors to show what exactly wrong.
   * If at least one valid image present, then create slideshow and return response with possible validation errors
   * for further processing API client processing.
   */
  @PostMapping
  public Mono<ResponseEntity<SlideshowResponseDTO>> createSlideshow(@RequestBody Mono<SlideshowRequestDTO> request) {

    return request.flatMap(r -> {
              Flux<ImageDetailsRequestDTO> images = Flux.fromIterable(r.images());

              Flux<ValidationError> validationErrors = validator
                      .validate(Mono.just(r))
                      .flatMapMany(Flux::fromIterable);

              Flux<ValidationErrorResponseDTO> validationErrorResponses = validationErrors
                      .map(error -> new ValidationErrorResponseDTO(error.code(), error.message()));

              Flux<ImageDetailsRequestDTO> validImages = filterValidImages(images, validationErrors);

              return Mono.zip(
                              validImages.collectList(),
                              validationErrorResponses.collectList()
                      )
                      .flatMap(tuple -> {
                        List<ImageDetailsRequestDTO> validImagesList = tuple.getT1();
                        List<ValidationErrorResponseDTO> errors = tuple.getT2();

                        //any valid image present
                        if (validImagesList.isEmpty()) {
                          return Mono.just(ResponseEntity.badRequest().body(new SlideshowResponseDTO(null, null, errors)));
                        }

                        return slideshowService.createSlideshow(convertToImageEntityList(validImagesList))
                                .map(slideshowProjection -> ResponseEntity.ok().body(
                                        new SlideshowResponseDTO(slideshowProjection.slideshowId(),
                                                objectMapper.convertValue(slideshowProjection.images(),
                                                        new TypeReference<>() {
                                                        }),
                                                errors)
                                ));

                      });

            })
            .onErrorResume(e ->
                    Mono.just(
                            ResponseEntity
                                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                    .body(new SlideshowResponseDTO(
                                            null,
                                            null,
                                            List.of(new ValidationErrorResponseDTO(
                                                    INTERNAL_SERVER_ERROR.getCode(),
                                                    INTERNAL_SERVER_ERROR.getDefaultMessage() +
                                                            " Unexpected error: " + e.getMessage()
                                            ))
                                    ))
                    ));

  }

  private Flux<ImageEntity> convertToImageEntityList(List<ImageDetailsRequestDTO> images) {
    return Mono.just(images)
            .flatMapMany(Flux::fromIterable)
            .map(imageDTO -> objectMapper.convertValue(imageDTO, ImageEntity.class));

  }
}
