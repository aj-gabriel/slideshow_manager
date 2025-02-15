package org.slideshow.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.domain.ProofOfPlayEventEntity;
import org.slideshow.model.dto.request.ImageDetailsRequestDTO;
import org.slideshow.model.dto.request.ProofOfPlayEventDTO;
import org.slideshow.model.dto.request.SlideshowRequestDTO;
import org.slideshow.model.dto.response.ImageResponseDTO;
import org.slideshow.model.dto.response.SlideshowResponseDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.model.projection.SlideshowProjection;
import org.slideshow.service.ProofOfPlayEventService;
import org.slideshow.service.SlideshowService;
import org.slideshow.service.SlideshowServiceFacade;
import org.slideshow.validation.ImagesValidationFacade;
import org.slideshow.validation.ValidationError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_SERVER_ERROR;
import static org.slideshow.validation.ValidationErrorCodes.INVALID_IMAGE_TYPE;
import static org.slideshow.validation.validators.ImageValidationService.IMAGE_HASH_KEY;
import static org.slideshow.validation.validators.ImageValidationService.INVALID_VALUE_KEY;
import static org.slideshow.web.SharedConstants.SLIDESHOW_API_PATH;

@ExtendWith(SpringExtension.class)
@WebFluxTest(SlideshowController.class)
public class SlideshowControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private SlideshowService slideshowService;

  @MockitoBean
  private ImagesValidationFacade validationFacade;

  @MockitoBean
  private SlideshowServiceFacade slideshowFacade;

  @MockitoBean
  private ProofOfPlayEventService proofOfPlayEventService;


  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  void shouldCreateNewSlideshowWithValidImages() {
    //prepare
    short duration = 10;
    String url = "test_url";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    List<ImageDetailsRequestDTO> imageDetailsList = List.of(new ImageDetailsRequestDTO(null, url, duration));
    SlideshowRequestDTO request = new SlideshowRequestDTO(imageDetailsList);
    SlideshowProjection slideshowDBProjection = new SlideshowProjection(1L, List.of(imageEntity));

    doReturn(Mono.empty()).when(validationFacade).validateImages(any());
    doReturn(Mono.just(slideshowDBProjection)).when(slideshowFacade).createSlideshow(any());

    //execute
    SlideshowResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), SlideshowRequestDTO.class)
            .exchange()
            .expectStatus().isOk()
            .expectBody(SlideshowResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals(objectMapper.convertValue(slideshowDBProjection.images(), new TypeReference<List<ImageResponseDTO>>() {
    }), result.images());
    assertEquals(Collections.emptyList(), result.errors());

    verify(validationFacade).validateImages(any());
    verify(slideshowFacade).createSlideshow(any());
  }

  @Test
  void shouldNotCreateNewSlideshowWithoutValidImages() {
    //prepare

    short duration = 10;
    String url = "test_url";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    List<ImageDetailsRequestDTO> imageDetailsList = List.of(new ImageDetailsRequestDTO(null, url, duration));
    SlideshowRequestDTO request = new SlideshowRequestDTO(imageDetailsList);
    ValidationError error = new ValidationError(INVALID_IMAGE_TYPE.getCode(),
            INVALID_IMAGE_TYPE.getDefaultMessage(),
            Map.of(IMAGE_HASH_KEY, imageEntity.hashCode(), INVALID_VALUE_KEY, url)
    );

    doReturn(Mono.just(List.of(error))).when(validationFacade).validateImages(any());

    //execute
    SlideshowResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), SlideshowRequestDTO.class)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(SlideshowResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertNull(result.id());
    assertNull(result.images());
    assertEquals(1, result.errors().size());
    assertEquals(new ValidationErrorResponseDTO(error.code(), error.message()), result.errors().get(0));

    verify(validationFacade).validateImages(any());
    verify(slideshowFacade, never()).createSlideshow(any());
  }

  @Test
  void shouldNotCreateNewSlideshowOnInternalError() {
    //prepare
    short duration = 10;
    String url = "test_url";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    List<ImageDetailsRequestDTO> imageDetailsList = List.of(new ImageDetailsRequestDTO(null, url, duration));
    SlideshowRequestDTO request = new SlideshowRequestDTO(imageDetailsList);

    doReturn(Mono.error(new RuntimeException())).when(validationFacade).validateImages(any());

    //execute
    SlideshowResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(request), SlideshowRequestDTO.class)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(SlideshowResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertNull(result.id());
    assertNull(result.images());
    assertEquals(1, result.errors().size());
    assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.errors().get(0).code());
    assertEquals("Something went wrong. Unexpected error: null", result.errors().get(0).message());

    verify(validationFacade).validateImages(any());
    verify(slideshowFacade, never()).createSlideshow(any());
  }

  @Test
  void shouldDeleteExistingSlideshow() {
    Long slideshowId = 1L;

    doReturn(Mono.empty()).when(slideshowService).deleteSlideshowById(any());

    webTestClient.delete()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH + "/{id}")
                    .build(slideshowId))
            .exchange()
            .expectStatus().isNoContent();

    verify(slideshowService).deleteSlideshowById(any());
  }

  @Test
  void shouldGetSlideshowInAscendingOrder() {
    //prepare
    long slideshowId = 1L;

    short duration = 10;
    String url = "test_url";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    SlideshowProjection slideshowDBProjection = new SlideshowProjection(slideshowId, List.of(imageEntity));

    doReturn(Mono.just(slideshowDBProjection)).when(slideshowService).getSlideshowById(slideshowId);

    //execute
    SlideshowResponseDTO result = webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH + "/{id}/slideshowOrder")
                    .build(slideshowId))
            .exchange()
            .expectStatus().isOk()
            .expectBody(SlideshowResponseDTO.class)
            .returnResult()
            .getResponseBody();

    // verify
    assertNotNull(result);
    assertEquals(slideshowId, result.id());
    assertEquals(1, result.images().size());
    assertEquals(objectMapper.convertValue(slideshowDBProjection.images(), new TypeReference<List<ImageResponseDTO>>() {
    }), result.images());

    verify(slideshowService).getSlideshowById(any());
  }

  @Test
  void shouldThrowExceptionOnGetSlideshowInAscendingOrder() {
    //prepare
    long slideshowId = 1L;

    short duration = 10;
    String url = "test_url";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    doReturn(Mono.error(new RuntimeException())).when(slideshowService).getSlideshowById(slideshowId);

    //execute
    SlideshowResponseDTO result = webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH + "/{id}/slideshowOrder")
                    .build(slideshowId))
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(SlideshowResponseDTO.class)
            .returnResult()
            .getResponseBody();

    // verify
    assertNotNull(result);
    assertNull(result.id());
    assertNull(result.images());

    assertEquals(1, result.errors().size());
    assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.errors().get(0).code());
    assertEquals("Something went wrong. Unexpected error: null", result.errors().get(0).message());

    verify(slideshowService).getSlideshowById(any());

  }

  @Test
  void proofOfPlay_ShouldLogEvent() {
    //prepare
    OffsetDateTime displayedAt = OffsetDateTime.now();
    OffsetDateTime replacedAt = displayedAt.plusSeconds(10);

    Long userId = 1235L;
    short actualDuration = 10;
    Long imageId = 1L;
    Long slideshowId = 1L;

    ProofOfPlayEventDTO eventDTO = new ProofOfPlayEventDTO(userId, displayedAt, replacedAt, actualDuration);

    ProofOfPlayEventEntity eventEntity =
            new ProofOfPlayEventEntity(1L, imageId, slideshowId, userId,
                    replacedAt, displayedAt, actualDuration);

    when(proofOfPlayEventService.recordProofOfPlay(Mono.just(eventEntity))).thenReturn(Mono.empty());

    //execute
    webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(SLIDESHOW_API_PATH + "/{id}/proof-of-play/{imageId}")
                    .build(slideshowId, imageId))
            .body(Mono.just(eventDTO), ProofOfPlayEventDTO.class)
            .exchange()
            .expectStatus().isOk();

    //verify
    verify(proofOfPlayEventService).recordProofOfPlay(any());
  }
}
