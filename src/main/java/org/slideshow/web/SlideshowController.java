package org.slideshow.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.ProofOfPlayEventEntity;
import org.slideshow.model.dto.request.ImageDetailsRequestDTO;
import org.slideshow.model.dto.request.ProofOfPlayEventDTO;
import org.slideshow.model.dto.request.SlideshowRequestDTO;
import org.slideshow.model.dto.response.SlideshowResponseDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.service.ProofOfPlayEventService;
import org.slideshow.service.SlideshowService;
import org.slideshow.service.SlideshowServiceFacade;
import org.slideshow.validation.ImagesValidationFacade;
import org.slideshow.validation.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_SERVER_ERROR;
import static org.slideshow.validation.ValidationUtils.filterValidImages;
import static org.slideshow.web.SharedConstants.SLIDESHOW_API_PATH;
import static org.springframework.http.HttpStatus.NO_CONTENT;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(SLIDESHOW_API_PATH)
public class SlideshowController {

  private final ObjectMapper objectMapper;
  private final SlideshowService slideshowService;
  private final SlideshowServiceFacade slideshowFacade;
  private final ImagesValidationFacade validationFacade;
  private final ProofOfPlayEventService proofOfPlayEventService;

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

              Flux<ValidationError> validationErrors = validationFacade
                      .validateImages(Mono.just(r))
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
                          return Mono.just(
                                  ResponseEntity
                                          .badRequest()
                                          .body(new SlideshowResponseDTO(null, null, errors))
                          );
                        }

                        return slideshowFacade.createSlideshow(convertToImageEntityList(validImagesList))
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

  //DELETE /deleteSlideshow/{id}: Remove a slideshow by its ID.
  @DeleteMapping("/{id}")
  public Mono<ResponseEntity<Void>> deleteSlideshow(@PathVariable("id") Long id) {
    return slideshowService.deleteSlideshowById(Mono.just(id))
            .thenReturn(ResponseEntity.status(NO_CONTENT).<Void>build())
            .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
  }


  //GET /slideShow/{id}/slideshowOrder: Retrieve images in a slideshow ordered by image addition date IN DESC order.
  @GetMapping("/{id}/slideshowOrder")
  public Mono<ResponseEntity<SlideshowResponseDTO>> getSlideshow(@PathVariable("id") Long id) {

    return slideshowService.getSlideshowById(id)
            .map(slideshowProjection -> ResponseEntity.ok().body(
                    new SlideshowResponseDTO(slideshowProjection.slideshowId(),
                            objectMapper.convertValue(slideshowProjection.images(),
                                    new TypeReference<>() {
                                    }),
                            null)
            ))
            .switchIfEmpty(Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build()))
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

  //POST /slideShow/{id}/proof-of-play/{imageId}: Record an event when an image is replaced by the next
  @PostMapping("/{id}/proof-of-play/{imageId}")
  public Mono<Void> logEvent(@PathVariable Long id,
                             @PathVariable Long imageId,
                             @RequestBody Mono<ProofOfPlayEventDTO> eventDTOMono) {
    return eventDTOMono
            .flatMap(dto -> Mono.just(buildProofOfPlayEvent(id, imageId, dto)))
            .flatMap(event -> proofOfPlayEventService.recordProofOfPlay(Mono.just(event)))
            .onErrorResume(e -> {
              log.error("An error occurred while logging event", e);
              return Mono.empty();
            })
            .then();//return empty.
  }

  private ProofOfPlayEventEntity buildProofOfPlayEvent(Long id, Long imageId, ProofOfPlayEventDTO dto) {

    ProofOfPlayEventEntity event = new ProofOfPlayEventEntity();
    event.setSlideshowId(id);
    event.setImageId(imageId);
    event.setUserId(dto.userId());
    event.setReplacedAt(dto.replacedAt());
    event.setDisplayedAt(dto.displayedAt());
    event.setActualDuration(dto.actualDuration());

    return event;
  }

  private Flux<ImageEntity> convertToImageEntityList(List<ImageDetailsRequestDTO> images) {
    return Mono.just(images)
            .flatMapMany(Flux::fromIterable)
            .map(imageDTO -> objectMapper.convertValue(imageDTO, ImageEntity.class));

  }
}
