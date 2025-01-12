package org.slideshow.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slideshow.model.domain.ImageEntity;
import org.slideshow.model.dto.request.ImageCreateRequestDTO;
import org.slideshow.model.dto.response.ImageCreationResponseDTO;
import org.slideshow.model.dto.response.ImageResponseDTO;
import org.slideshow.model.dto.response.ValidationErrorResponseDTO;
import org.slideshow.model.projection.ImageProjection;
import org.slideshow.service.ImageService;
import org.slideshow.service.SlideshowServiceFacade;
import org.slideshow.validation.ImagesValidationFacade;
import org.slideshow.validation.ValidationErrorCodes;
import org.slideshow.validation.validators.ImageValidationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.slideshow.validation.ValidationErrorCodes.INTERNAL_SERVER_ERROR;
import static org.slideshow.web.SharedConstants.IMAGES_API_PATH;

@ExtendWith(SpringExtension.class)
@WebFluxTest(ImagesController.class)
public class ImagesControllerTest {

  @Autowired
  private WebTestClient webTestClient;

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private ImageService imageService;

  @MockitoBean
  private ImagesValidationFacade validationFacade;

  @MockitoBean
  private ImageValidationService validator;

  @MockitoBean
  private SlideshowServiceFacade slideshowServiceFacade;


  @BeforeEach
  public void setUp() {
  }

  @AfterEach
  public void tearDown() throws Exception {
  }

  @Test
  void shouldCreateNewImage() {
    //prepare
    short duration = 10;
    String url = "test_url";
    ImageCreateRequestDTO requestDTO = new ImageCreateRequestDTO(url, duration);
    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    doReturn(Mono.empty()).when(validationFacade).validateRequest(any());
    doReturn(Mono.just(imageEntity)).when(imageService).createImage(any());

    //execute
    ImageCreationResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(IMAGES_API_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(requestDTO), ImageCreateRequestDTO.class)
            .exchange()
            .expectStatus().isCreated()
            .expectBody(ImageCreationResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertEquals(1L, result.id());
    assertEquals(duration, result.duration());
    assertEquals(url, result.url());

    verify(validationFacade).validateRequest(any());
    verify(imageService).createImage(any());

  }

  @Test
  void shouldNotCreateNewImageDueInvalidUrl() {
    //prepare
    short duration = 10;
    String url = "test_url";
    ImageCreateRequestDTO requestDTO = new ImageCreateRequestDTO(url, duration);
    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    doReturn(Mono.just(new ValidationErrorResponseDTO(
            ValidationErrorCodes.INVALID_IMAGE_URL.getCode(),
            ValidationErrorCodes.INVALID_IMAGE_URL.getDefaultMessage()
    ))).when(validationFacade).validateRequest(any());

    //execute
    ImageCreationResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(IMAGES_API_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(requestDTO), ImageCreateRequestDTO.class)
            .exchange()
            .expectStatus().isBadRequest()
            .expectBody(ImageCreationResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertNull(result.id());
    assertNull(result.duration());
    assertNull(result.url());

    verify(validationFacade).validateRequest(any());
    verify(imageService, never()).createImage(any());

  }

  @Test
  void shouldReturnInternalServerErrorOneNewImageCreate() {
    //prepare
    short duration = 10;
    String url = "test_url";
    ImageCreateRequestDTO requestDTO = new ImageCreateRequestDTO(url, duration);
    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    doReturn(Mono.error(new RuntimeException())).when(validationFacade).validateRequest(any());

    //execute
    ImageCreationResponseDTO result = webTestClient.post()
            .uri(uriBuilder -> uriBuilder
                    .path(IMAGES_API_PATH)
                    .build())
            .accept(MediaType.APPLICATION_JSON)
            .body(Mono.just(requestDTO), ImageCreateRequestDTO.class)
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBody(ImageCreationResponseDTO.class)
            .returnResult()
            .getResponseBody();

    //verify
    assertNotNull(result);
    assertNull(result.id());
    assertNull(result.duration());
    assertNull(result.url());

    assertEquals(1, result.errors().size());
    assertEquals(INTERNAL_SERVER_ERROR.getCode(), result.errors().get(0).code());
    assertEquals("Something went wrong. Unexpected error: null", result.errors().get(0).message());

    verify(validationFacade).validateRequest(any());
    verify(imageService, never()).createImage(any());

  }

  @Test
  void shouldDeleteImageByIdSuccessfully() {
    // prepare
    long idToDelete = 1L;

    when(slideshowServiceFacade.deleteImageAndUpdateSlideshow(any()))
            .thenReturn(Mono.empty());

    // execute
    webTestClient.delete()
            .uri(IMAGES_API_PATH + "/" + idToDelete)
            .exchange()
            .expectStatus().isOk();

    // verify
    verify(slideshowServiceFacade).deleteImageAndUpdateSlideshow(any());
  }

  @Test
  void shouldHandleFailureWhenDeletingImageById() {
    // prepare
    long idToDelete = 1L;

    when(slideshowServiceFacade.deleteImageAndUpdateSlideshow(any()))
            .thenThrow(new RuntimeException("some runtime exception"));

    // execute
    webTestClient.delete()
            .uri(IMAGES_API_PATH + "/" + idToDelete)
            .exchange()
            .expectStatus().is5xxServerError();

    // verify
    verify(slideshowServiceFacade, times(1)).deleteImageAndUpdateSlideshow(any());
  }

  @Test
  void shouldSuccessfullySearchImages() {
    // prepare
    long imageId = 1L;
    short duration = 10;
    String url = "test_url";
    String keyword = "test";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    when(imageService.findByKeywordAndDuration(anyString(), anyInt(), any()))
            .thenReturn(Flux.just(new ImageProjection(imageId, url, duration)));

    // execute
    webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(IMAGES_API_PATH + "/search")
                    .queryParam("keyword", keyword)
                    .queryParam("duration", String.valueOf(duration))
                    .queryParam("direction", "ASC")
                    .build()
            )
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(ImageResponseDTO.class)
            .hasSize(1)
            .contains(new ImageResponseDTO(imageId, url, duration));

    // verify
    verify(imageService).findByKeywordAndDuration(keyword, 10, Sort.Direction.ASC);
  }

  @Test
  void shouldReturnErrorOnSearchImages() {
    // prepare
    long imageId = 1L;
    short duration = 10;
    String url = "test_url";
    String keyword = "test";

    ImageEntity imageEntity = new ImageEntity();
    imageEntity.setId(1L);
    imageEntity.setDuration(duration);
    imageEntity.setUrl(url);

    when(imageService.findByKeywordAndDuration(anyString(), anyInt(), any()))
            .thenReturn(Flux.error(new RuntimeException()));

    // execute
    webTestClient.get()
            .uri(uriBuilder -> uriBuilder
                    .path(IMAGES_API_PATH + "/search")
                    .queryParam("keyword", keyword)
                    .queryParam("duration", String.valueOf(duration))
                    .queryParam("direction", "ASC")
                    .build()
            )
            .exchange()
            .expectStatus().is5xxServerError()
            .expectBodyList(ImageResponseDTO.class)
            .hasSize(0);

    // verify
    verify(imageService).findByKeywordAndDuration(keyword, 10, Sort.Direction.ASC);
  }

}
