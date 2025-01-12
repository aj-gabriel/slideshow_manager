package org.slideshow.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.dto.request.ImageCreateRequestDTO;
import org.slideshow.model.dto.response.ImageCreationResponseDTO;
import org.slideshow.model.dto.response.ImageResponseDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.service.ImageService;
import org.slideshow.service.SlideshowServiceFacade;
import org.slideshow.validation.ImageValidationException;
import org.slideshow.validation.ImagesValidationFacade;
import org.slideshow.validation.validators.ImageValidationService;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_SERVER_ERROR;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ImagesController.API_V1_IMAGES_PATH)
public class ImagesController {

  public static final String API_V1_IMAGES_PATH = "/api/v1/images";
  private final ImageService imageService;
  private final ObjectMapper objectMapper;
  private final ImageValidationService validator;
  private final ImagesValidationFacade validationFacade;
  private final SlideshowServiceFacade slideshowServiceFacade;

  @PostMapping
  public Mono<ResponseEntity<ImageCreationResponseDTO>> createImage(@RequestBody Mono<ImageCreateRequestDTO> request) {
    return request.flatMap(r ->
                    validationFacade.validateRequest(r)
                            .flatMap(error -> Mono.error(new ImageValidationException(error)))
                            .switchIfEmpty(Mono.just(r))//transfer request data to downstream
                            .flatMap(data ->
                                    {
                                      log.info("on create {}", data.toString());
                                      return imageService.createImage(Mono.just(objectMapper.convertValue(data, ImageEntity.class)))
                                              .map(image -> ResponseEntity.status(HttpStatus.CREATED).body(
                                                      new ImageCreationResponseDTO(image.getId(), image.getUrl(), image.getDuration(), null)
                                              ));
                                    }
                            )
            )
            .onErrorResume(ImageValidationException.class,
                    ex -> Mono.just(ResponseEntity.badRequest().body(
                            new ImageCreationResponseDTO(null, null, null, List.of(ex.getError()))))
            )
            .onErrorResume(e -> Mono
                    .just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ImageCreationResponseDTO(null, null, null,
                            List.of(new ValidationErrorResponseDTO(
                                    INTERNAL_SERVER_ERROR.getCode(),
                                    INTERNAL_SERVER_ERROR.getDefaultMessage() +
                                            " Unexpected error: " + e.getMessage()))

                    ))));
  }

  @GetMapping("/images/search")
  public Flux<ResponseEntity<ImageResponseDTO>> searchImages(
          @RequestParam(value = "keyword", required = false) String keyword,
          @RequestParam(value = "duration", required = false) Integer duration,
          @RequestParam(value = "direction", required = false, defaultValue = "ASC") String direction) {

    Sort.Direction sortDirection;

    try {
      sortDirection = Sort.Direction.valueOf(direction.toUpperCase());
    } catch (IllegalArgumentException e) {
      return Flux.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build());
    }

    return imageService.findByKeywordAndDuration(keyword, duration, sortDirection)
            .map(imageProjection -> ResponseEntity.ok().body(
                            new ImageResponseDTO(
                                    imageProjection.id(),
                                    imageProjection.url(),
                                    imageProjection.duration())
                    )
            )
            .switchIfEmpty(Flux.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
            .onErrorResume(e -> Flux.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
  }

  @DeleteMapping("/{id}")
  public Mono<Void> deleteById(@PathVariable Long id) {
    return slideshowServiceFacade.deleteImageAndUpdateSlideshow(Mono.just(id));
  }

}
